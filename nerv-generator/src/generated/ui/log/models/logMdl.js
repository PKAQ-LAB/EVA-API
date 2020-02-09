import { fetch, del, save, get, } from '../services/logSvc';

export default {
  namespace: 'log',
  state: {
    currentItem: {},
    // 新增、查看、编辑
    modalType: '',
    formValues: {},
  },
  effects: {
    // 加载列表数据
    *fetch({ payload }, { call, put }) {
      const response = yield call(fetch, payload);
      if (response && response.success) {
        yield put({
          type: 'updateState',
          payload: {
            selectedRowKeys: [],
            log: response.data,
          },
        });
      }
    },
    // 编辑按钮
    *edit({ payload }, { call, put }) {
      const response = yield call(get, payload);
      if (response && response.success) {
        yield put({
          type: 'updateState',
          payload: {
            modalType: 'edit',
            currentItem: response.data,
          },
        });
      }
    },
    // 删除
    *remove({ payload }, { call, put }) {
      const response = yield call(del, payload);
      if (response && response.success) {
        yield put({
          type: 'updateState',
          payload: {
            roles: response.data,
            selectedRowKeys: [],
          },
        });
      }
    },
    // 保存提交
    *save({ payload }, { call, put }) {
      const response = yield call(save, payload);
      if (response && response.success) {
        yield put({
          type: 'updateState',
          payload: {
            modalType: '',
            currentItem: {},
            roles: response.data,
          },
        });
      }
    },
  },
  reducers: {
    updateState(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    },
  },
};
