"""只失效本次已证明归属的三个开发缓存键；凭据仅从开发配置读取。"""
import argparse
import base64
import hashlib
import json
import pathlib
import re
import socket

if not __debug__:
    raise SystemExit('禁止使用 -O 绕过维护脚本校验')

ROOT = pathlib.Path(__file__).resolve().parents[2]
KEYS = ['dictdata::RESOURCE_TYPE', 'eva:security:tenant:0:role-resource:766008469304664561',
        'eva:security:tenant:0:role-resource:1000000000000000004']
FINGERPRINT = 'c2a1d585223c'


def configuration(profile):
    text = (ROOT / f'web-booter/src/main/resources/application-{profile}.yaml').read_text(encoding='utf-8')
    return {key: value.strip().strip('"\'') for key, value in
            re.findall(r'^      (host|port|database|password):[ \t]*(.*)$', text, re.M)}


class Redis:
    def __init__(self, conf):
        self.sock = socket.create_connection((conf['host'], int(conf['port'])), timeout=8)
        self.stream = self.sock.makefile('rb')
        if conf.get('password'):
            self.command('AUTH', conf['password'])
        self.command('SELECT', conf['database'])

    def response(self):
        line = self.stream.readline()
        if not line:
            raise RuntimeError('redis response missing')
        kind, value = line[:1], line[1:-2]
        if kind == b'-':
            raise RuntimeError('redis command rejected')
        if kind == b'+':
            return value
        if kind == b':':
            return int(value)
        if kind == b'$':
            length = int(value)
            if length == -1:
                return None
            payload = self.stream.read(length)
            assert self.stream.read(2) == b'\r\n'
            return payload
        if kind == b'*':
            return [self.response() for _ in range(int(value))]
        raise RuntimeError('unsupported response')

    def command(self, *arguments):
        parts = [part if isinstance(part, bytes) else str(part).encode() for part in arguments]
        self.sock.sendall(b'*' + str(len(parts)).encode() + b'\r\n' + b''.join(
            b'$' + str(len(part)).encode() + b'\r\n' + part + b'\r\n' for part in parts))
        return self.response()


def run(backup_directory, apply):
    directory = pathlib.Path(backup_directory).resolve()
    assert directory.parent == (ROOT / '.gradle').resolve()
    assert (directory / 'committed.json').is_file(), 'DB transaction must be committed before cache invalidation'
    dev, prod = configuration('dev'), configuration('prod')
    target = tuple(dev.get(key) for key in ['host', 'port', 'database'])
    assert target != tuple(prod.get(key) for key in ['host', 'port', 'database'])
    assert not any('$' in value for value in dev.values())
    fingerprint = hashlib.sha256((dev['host'] + ':' + dev['port'] + '/' + dev['database']).encode()).hexdigest()[:12]
    assert fingerprint == FINGERPRINT
    before_file = ROOT / '.gradle/module-resource-cache-before.json'
    backup = json.loads(before_file.read_text(encoding='utf-8'))
    assert backup['fingerprint'] == FINGERPRINT
    assert [row['key'] for row in backup['keys']] == KEYS
    assert all(row['pttl'] == -1 for row in backup['keys'])
    dumps = [base64.b64decode(row['dump']) for row in backup['keys']]
    assert all(hashlib.sha256(payload).hexdigest() == row['sha256'] for row, payload in zip(backup['keys'], dumps))
    copied = directory / 'redis-before.json'
    copied.write_bytes(before_file.read_bytes())
    assert copied.read_bytes() == before_file.read_bytes()
    client = Redis(dev)
    present = [client.command('EXISTS', key) for key in KEYS]
    if apply:
        # 全部键仍与备份相同才一次性删除，任何并发改动都拒绝执行。
        script = "for i=1,#KEYS do if redis.call('DUMP',KEYS[i]) ~= ARGV[i] then return -1 end end " \
                 "return redis.call('DEL',unpack(KEYS))"
        count = client.command('EVAL', script, len(KEYS), *KEYS, *dumps)
        assert count == 3, 'keys changed after ownership verification; no deletion permitted'
        result = {'fingerprint': fingerprint, 'deleted': count, 'keys': KEYS,
                  'post_exists': [client.command('EXISTS', key) for key in KEYS],
                  'backup_sha256': hashlib.sha256(copied.read_bytes()).hexdigest()}
        (directory / 'redis-invalidated.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    else:
        result = {'fingerprint': fingerprint, 'applied': False, 'keys': KEYS, 'exists': present}
    client.sock.close()
    print(json.dumps(result))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('backup_directory')
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    try:
        run(args.backup_directory, args.apply)
    except Exception as error:
        print(json.dumps({'cache_status': 'requires_review', 'error_class': type(error).__name__}))
        raise SystemExit(2)
