import { stringify } from 'qs';
import request from '@/utils/request';
import { getNoUndefinedString } from '@/utils/utils';
// 根据id获取日志信息
export async function get(params) {
;
  return request(`/api/sys/log/get/${getNoUndefinedString(params.id)}`);
}

// 查询日志列表}
export async function fetch(params) {
  return request(`/api/sys/log/list?${stringify(params)}`);
}

// 根据ID删除日志
export async function del(params) {
  return request('/api/sys/log/del', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

// 保存日志信息
export async function save(params) {
  return request('/api/sys/log/save', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}