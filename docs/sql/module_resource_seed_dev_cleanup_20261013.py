"""本次开发库资源种子与开发工具清理；默认只读计划，显式 --apply 才执行。"""
import argparse
import datetime
import hashlib
import json
import pathlib
import re
import urllib.parse

import psycopg2
from psycopg2 import sql
from psycopg2.extras import RealDictCursor

if not __debug__:
    raise SystemExit('禁止使用 -O 绕过维护脚本校验')

ROOT = pathlib.Path(__file__).resolve().parents[2]
TARGET = 'd22bc534e71c'
DEV_IDS = [1000000000000000300, 1000000000000000301, 1000000000000000302]
DEV_RESOURCE_IDS = [1000000000000001300, 1000000000000001301,
                    1000000000000001302, 1000000000000001303]
DICT_IDS = [1000000000000004057, 1000000000000004058]
TABLES = ['sys_module', 'sys_module_resources', 'sys_roleres_ref', 'sys_tenant_resource',
          'sys_tenant_package_resource', 'sys_role_resource', 'sys_dict_item']
SOURCE = 'eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/'
CONTROLLERS = {
    'user/ctrl/UserCtrl.java': ('sys.account', '账号管理'),
    'organization/ctrl/OrganizationCtrl.java': ('sys.organization', '组织管理'),
    'post/ctrl/PostCtrl.java': ('sys.organization', '岗位管理'),
    'role/ctrl/RoleCtrl.java': ('sys.role', '角色管理'),
    'module/ctrl/ModuleCtrl.java': ('sys.module', '模块管理'),
    'dict/ctrl/DictCtrl.java': ('sys.dictionary', '字典管理'),
    'log/biz/ctrl/BizLogCtrl.java': ('log.biz', '业务日志'),
    'log/error/ctrl/ErrorCtrl.java': ('log.error', '错误日志'),
}


def config(profile):
    text = (ROOT / f'web-booter/src/main/resources/application-{profile}.yaml').read_text(encoding='utf-8')
    values = {}
    inside = False
    for line in text.splitlines():
        if line == '  datasource:':
            inside = True
            continue
        if inside and line and not line.startswith('    '):
            inside = False
        match = re.match(r'^    (url|username|password):\s*(.*)', line) if inside else None
        if match:
            values[match.group(1)] = match.group(2).strip().strip('"\'')
    return values


def connect(readonly=True):
    dev, prod = config('dev'), config('prod')
    uri = urllib.parse.urlparse(dev['url'].replace('jdbc:p6spy:', '').replace('jdbc:', ''))
    other = urllib.parse.urlparse(prod['url'].replace('jdbc:p6spy:', '').replace('jdbc:', ''))
    fingerprint = hashlib.sha256((uri.hostname + ':' + str(uri.port) + uri.path).encode()).hexdigest()[:12]
    assert fingerprint == TARGET and uri.scheme == 'postgresql' and uri.path == '/eva'
    assert (uri.hostname, uri.port, uri.path) != (other.hostname, other.port, other.path)
    assert urllib.parse.parse_qs(uri.query).get('currentSchema') == ['eva']
    assert not any('${' in value for value in dev.values())
    conn = psycopg2.connect(host=uri.hostname, port=uri.port or 5432, dbname='eva',
                            user=dev['username'], password=dev['password'], connect_timeout=8,
                            application_name='codex-module-resource-maintenance',
                            options='-c statement_timeout=15000 -c lock_timeout=5000')
    conn.set_session(readonly=readonly, isolation_level='SERIALIZABLE')
    return conn


def routes():
    result = []
    for relative, (module, title) in CONTROLLERS.items():
        file = ROOT / SOURCE / relative
        original = file.read_text(encoding='utf-8')
        text = re.sub(r'/\*.*?\*/', '', original, flags=re.S)
        text = re.sub(r'^\s*//.*$', '', text, flags=re.M)
        prefix = re.search(r'@RequestMapping\("([^"\n]+)"\)', text).group(1)
        mappings = list(re.finditer(r'@(Get|Post|Put|Delete|Patch)Mapping\(([^)]*)\)', text))
        for index, mapping in enumerate(mappings):
            paths = re.findall(r'"([^"\n]*)"', mapping.group(2))
            assert len(paths) == 1, 'ambiguous mapping'
            segment = text[mapping.end():mappings[index + 1].start() if index + 1 < len(mappings) else len(text)]
            function = re.search(r'public\s+[\w<>?, .\[\]]+\s+(\w+)\s*\(', segment).group(1)
            summary = re.search(r'@Operation\(summary\s*=\s*"([^"]+)"', segment)
            method = mapping.group(1).upper()
            path = prefix + paths[0]
            assert '/**' not in path and not path.startswith('/api/') and method in ['GET', 'POST']
            code = 'HTTP_' + file.stem.replace('Ctrl', '').upper() + '_' + method + '_' + function.upper()
            result.append({'id': 1000000000000011000 + len(result), 'module_code': module,
                           'code': code, 'resource_type': method, 'resource_url': path,
                           'resource_desc': title + '：' + (summary.group(1) if summary else function),
                           'source': str(file.relative_to(ROOT)).replace('\\', '/'),
                           'source_sha256': hashlib.sha256(file.read_bytes()).hexdigest()})
    assert len({(r['module_code'], r['resource_type'], r['resource_url']) for r in result}) == len(result)
    assert len({r['code'] for r in result}) == len(result)
    for prefix in ['/sys/account', '/sys/organization', '/sys/post', '/sys/role', '/sys/module', '/sys/dictionary']:
        for action in ['list', 'edit', 'del']:
            assert any(r['resource_url'] == prefix + '/' + action
                       or r['resource_url'].startswith(prefix + '/' + action + '/') for r in result)
    return result


def rows(cursor, table, where='TRUE', params=()):
    cursor.execute(sql.SQL('SELECT * FROM eva.{} WHERE ' + where).format(sql.Identifier(table)), params)
    return [dict(row) for row in cursor.fetchall()]


def snapshot(cursor):
    data = {}
    data['sys_module'] = rows(cursor, 'sys_module', 'id = ANY(%s)', (DEV_IDS,))
    data['sys_module_resources'] = rows(cursor, 'sys_module_resources', 'main_id = ANY(%s)', (DEV_IDS,))
    for table in ['sys_roleres_ref', 'sys_tenant_resource', 'sys_tenant_package_resource']:
        data[table] = rows(cursor, table, 'resource_id = ANY(%s)', (DEV_RESOURCE_IDS,))
    paths = [(row['resource_url'], row['resource_type']) for row in data['sys_module_resources']]
    pairs = {(row['role_id'], url, method) for row in data['sys_roleres_ref']
             for url, method in paths if any(resource['id'] == row['resource_id']
                                            and resource['resource_url'] == url
                                            and resource['resource_type'] == method
                                            for resource in data['sys_module_resources'])}
    data['sys_role_resource'] = [row for row in rows(cursor, 'sys_role_resource')
                                 if (row['role_id'], row['resource_path'], row['http_method']) in pairs]
    data['sys_dict_item'] = rows(cursor, 'sys_dict_item', 'id = ANY(%s)', (DICT_IDS,))
    return data


def checks(cursor, data):
    cursor.execute("SELECT current_database() AS database, pg_is_in_recovery() AS recovery")
    assert dict(cursor.fetchone()) == {'database': 'eva', 'recovery': False}
    cursor.execute("SELECT DISTINCT table_schema FROM information_schema.tables "
                   "WHERE table_name IN ('sys_module','sys_module_resources')")
    assert {row['table_schema'] for row in cursor.fetchall()} == {'eva'}
    cursor.execute("SELECT table_type FROM information_schema.tables WHERE table_schema='eva' "
                   "AND table_name='sys_role_module'")
    assert cursor.fetchone()['table_type'] == 'VIEW'
    cursor.execute("SELECT count(*) AS count FROM eva.sys_module_resources "
                   "WHERE resource_type IS NULL OR upper(resource_type) NOT IN ('*','GET','POST','PUT','DELETE','PATCH')")
    assert cursor.fetchone()['count'] == 0, 'non-HTTP resource dependencies need a decision'
    if data['sys_module']:
        expected = [('dev', '/dev', 0), ('dev.generator', '/dev/generator', DEV_IDS[0]),
                    ('dev.workflow', '/dev/workflow', DEV_IDS[0])]
        actual = sorted(data['sys_module'], key=lambda row: row['id'])
        assert [(r['code'], r['route_url'], r['pid']) for r in actual] == expected
        assert {r['id'] for r in data['sys_module_resources']} == set(DEV_RESOURCE_IDS)
        assert all(row['tenant_id'] == 0 and row['deleted'] == 0 for row in actual)
        cursor.execute('SELECT id FROM eva.sys_module WHERE pid = ANY(%s) AND NOT id = ANY(%s)', (DEV_IDS, DEV_IDS))
        assert not cursor.fetchall(), 'unexpected descendant'
        assert {k: len(v) for k, v in data.items()} == {
            'sys_module': 3, 'sys_module_resources': 4, 'sys_roleres_ref': 8,
            'sys_tenant_resource': 4, 'sys_tenant_package_resource': 0,
            'sys_role_resource': 8, 'sys_dict_item': 2}
        assert [(r['id'], r['d_code'], r['main_id']) for r in sorted(data['sys_dict_item'], key=lambda r: r['id'])] == [
            (DICT_IDS[0], 'BUTTON', 1000000000000004050),
            (DICT_IDS[1], 'OPERATE', 1000000000000004050)]
    else:
        assert all(not value for value in data.values()), 'partial cleanup detected'


def literals(cursor, row):
    columns = sql.SQL(',').join(sql.Identifier(column) for column in row)
    values = sql.SQL(',').join(sql.Placeholder() for _ in row)
    return columns, values, tuple(row.values())


def backup(cursor, data, seeds):
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    destination = ROOT / '.gradle' / ('module-resource-backup-' + stamp)
    destination.mkdir(exist_ok=False)
    backup_file = destination / 'rows.json'
    backup_file.write_text(json.dumps(data, default=str, ensure_ascii=False, indent=2), encoding='utf-8')
    assert json.loads(backup_file.read_text(encoding='utf-8')) == json.loads(json.dumps(data, default=str))
    restore = ['-- 本次精确备份恢复；出现冲突时停止，不覆盖后续修改。', 'BEGIN;',
               "DO $$ BEGIN IF current_database() <> 'eva' THEN RAISE EXCEPTION 'wrong database'; END IF; END $$;"]
    restore.append('LOCK TABLE ' + ','.join('eva.' + table for table in TABLES)
                   + ' IN SHARE ROW EXCLUSIVE MODE;')
    for seed in seeds:
        restore.append(cursor.mogrify("DO $$ BEGIN IF EXISTS (SELECT 1 FROM eva.sys_roleres_ref WHERE resource_id=%s) "
                                      "OR EXISTS (SELECT 1 FROM eva.sys_tenant_resource WHERE resource_id=%s) "
                                      "OR EXISTS (SELECT 1 FROM eva.sys_tenant_package_resource WHERE resource_id=%s) "
                                      "THEN RAISE EXCEPTION 'seed acquired new grants; review before restore'; END IF; END $$;",
                                      (seed['id'], seed['id'], seed['id'])).decode())
    for seed in seeds:
        restore.append(cursor.mogrify('DELETE FROM eva.sys_module_resources WHERE id=%s AND code=%s '
                                     'AND resource_url=%s AND resource_type=%s;',
                                     (seed['id'], seed['code'], seed['resource_url'], seed['resource_type'])).decode())
    for table in ['sys_dict_item', 'sys_module', 'sys_module_resources', 'sys_roleres_ref',
                  'sys_tenant_resource', 'sys_tenant_package_resource', 'sys_role_resource']:
        for row in data[table]:
            columns, values, args = literals(cursor, row)
            statement = sql.SQL('INSERT INTO eva.{} ({}) VALUES ({});').format(sql.Identifier(table), columns, values)
            restore.append(cursor.mogrify(statement, args).decode())
    restore.append('COMMIT;')
    (destination / 'restore.sql').write_text('\n'.join(restore) + '\n', encoding='utf-8')
    digests = {file.name: hashlib.sha256(file.read_bytes()).hexdigest() for file in destination.iterdir()}
    (destination / 'sha256.json').write_text(json.dumps(digests, indent=2), encoding='utf-8')
    assert all(hashlib.sha256((destination / name).read_bytes()).hexdigest() == digest
               for name, digest in digests.items())
    return destination


def run(apply):
    manifest = routes()
    conn = connect(readonly=not apply)
    backup_dir = None
    with conn:
        with conn.cursor(cursor_factory=RealDictCursor) as cursor:
            if apply:
                cursor.execute(sql.SQL('LOCK TABLE {} IN SHARE ROW EXCLUSIVE MODE').format(
                    sql.SQL(',').join(sql.Identifier('eva', table) for table in TABLES)))
            before = snapshot(cursor)
            checks(cursor, before)
            modules = rows(cursor, 'sys_module', 'deleted=0')
            module_ids = {row['code']: row['id'] for row in modules}
            for seed in manifest:
                assert seed['module_code'] in module_ids
                seed['main_id'] = module_ids[seed['module_code']]
            original_refs = {table: rows(cursor, table) for table in TABLES if table != 'sys_module_resources'}
            existing = rows(cursor, 'sys_module_resources')
            inserts = []
            for seed in manifest:
                matches = [r for r in existing if r['main_id'] == seed['main_id'] and r['deleted'] == 0
                           and (r['code'] == seed['code'] or (r['resource_url'], r['resource_type']) ==
                                (seed['resource_url'], seed['resource_type']))]
                if matches:
                    assert len(matches) == 1 and matches[0]['code'] == seed['code']
                    assert (matches[0]['resource_url'], matches[0]['resource_type']) == (
                        seed['resource_url'], seed['resource_type'])
                    continue
                assert not any(r['id'] == seed['id'] for r in existing), 'seed ID collision'
                inserts.append(seed)
            summary = {'target_fingerprint': TARGET, 'schema': 'eva', 'mode': 'standalone',
                       'cleanup_counts': {k: len(v) for k, v in before.items()},
                       'seed_count': len(manifest), 'to_insert': len(inserts), 'applied': apply}
            if apply:
                backup_dir = backup(cursor, before, inserts)
                for table in ['sys_roleres_ref', 'sys_tenant_resource', 'sys_tenant_package_resource']:
                    cursor.execute(sql.SQL('DELETE FROM eva.{} WHERE resource_id=ANY(%s)').format(sql.Identifier(table)),
                                   (DEV_RESOURCE_IDS,))
                    assert cursor.rowcount == len(before[table])
                flat_ids = [row['id'] for row in before['sys_role_resource']]
                cursor.execute('DELETE FROM eva.sys_role_resource WHERE id=ANY(%s)', (flat_ids,))
                assert cursor.rowcount == len(flat_ids)
                cursor.execute('DELETE FROM eva.sys_module_resources WHERE main_id=ANY(%s)', (DEV_IDS,))
                assert cursor.rowcount == len(before['sys_module_resources'])
                cursor.execute('DELETE FROM eva.sys_module WHERE id=ANY(%s)', (DEV_IDS,))
                assert cursor.rowcount == len(before['sys_module'])
                cursor.execute('DELETE FROM eva.sys_dict_item WHERE id=ANY(%s)', (DICT_IDS,))
                assert cursor.rowcount == len(before['sys_dict_item'])
                for position, seed in enumerate(inserts):
                    cursor.execute('INSERT INTO eva.sys_module_resources '
                                   '(id,main_id,tenant_id,code,resource_desc,resource_url,resource_type,sort,deleted,'
                                   'create_by,utc_create,modify_by,utc_modify) '
                                   "VALUES (%s,%s,0,%s,%s,%s,%s,%s,0,'codex-db-maintenance',"
                                   "CURRENT_TIMESTAMP,'codex-db-maintenance',CURRENT_TIMESTAMP)",
                                   (seed['id'], seed['main_id'], seed['code'], seed['resource_desc'],
                                    seed['resource_url'], seed['resource_type'], 100 + position))
                assert all(not value for value in snapshot(cursor).values())
                final_resources = rows(cursor, 'sys_module_resources')
                for original in existing:
                    if original['id'] not in DEV_RESOURCE_IDS:
                        assert original in final_resources, 'unrelated resource changed'
                for table, originals in original_refs.items():
                    expected = [row for row in originals if row not in before[table]]
                    actual = rows(cursor, table)
                    assert len(actual) == len(expected) and all(row in actual for row in expected), table
                assert len(final_resources) == len(existing) - len(before['sys_module_resources']) + len(inserts)
                for seed in manifest:
                    assert sum(r['deleted'] == 0 and r['main_id'] == seed['main_id'] and r['code'] == seed['code']
                               and r['resource_url'] == seed['resource_url'] and r['resource_type'] == seed['resource_type']
                               for r in final_resources) == 1
                cursor.execute('SAVEPOINT restore_rehearsal')
                restore_text = (backup_dir / 'restore.sql').read_text(encoding='utf-8')
                restore_body = '\n'.join(line for line in restore_text.splitlines()
                                         if line.strip() not in ['BEGIN;', 'COMMIT;'])
                cursor.execute(restore_body)
                assert snapshot(cursor) == before
                restored_resources = rows(cursor, 'sys_module_resources')
                assert len(restored_resources) == len(existing) and all(r in restored_resources for r in existing)
                for table, originals in original_refs.items():
                    restored_rows = rows(cursor, table)
                    assert len(restored_rows) == len(originals) and all(r in restored_rows for r in originals), table
                cursor.execute('ROLLBACK TO SAVEPOINT restore_rehearsal')
                cursor.execute('RELEASE SAVEPOINT restore_rehearsal')
                assert all(not value for value in snapshot(cursor).values())
                summary['restore_rehearsal'] = 'passed within savepoint, rolled back to applied state'
                summary['backup_path'] = str(backup_dir)
            destination = ROOT / '.gradle' / 'module-resource-plan.json'
            destination.write_text(json.dumps({'summary': summary, 'routes': manifest}, ensure_ascii=False, indent=2),
                                   encoding='utf-8')
    conn.close()
    if backup_dir:
        (backup_dir / 'committed.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
    print(json.dumps(summary, ensure_ascii=False))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    try:
        run(args.apply)
    except Exception as error:
        print(json.dumps({'applied': False, 'transaction': 'rolled_back_or_not_started',
                          'error_class': type(error).__name__}))
        raise SystemExit(2)
