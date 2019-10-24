import React from 'react';
import { connect } from 'dva';
import { Button, Divider,  } from 'antd';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import { Form, Input } from 'antx';
import List from './list.jsx.vm';
import AOEForm from './aoeform';
/**
 * 角色（权限）管理 主界面
 */
@Form.create()
@connect(state => ({
  log: state.log,
  loading: state.loading.log,
}))
export default class Role extends React.PureComponent {
  // 新增窗口
  handlAddClick = () => {
    this.props.dispatch({
      type: 'log/updateState',
      payload: {
        modalType: 'create',
        currentItem: {},
      },
    });
  };

  // 重置事件
  handleFormReset = () => {
    const { form, dispatch } = this.props;
    form.resetFields();
    dispatch({
      type: 'log/fetch',
    });
  };

  // 搜索事件
  handleSearch = () => {
    const { dispatch, form } = this.props;
    form.validateFields((err, fieldsValue) => {
      if (err) return;

      const values = {
        ...fieldsValue,
      };

      dispatch({
        type: 'log/fetch',
        payload: values,
      });
    });
  };

    // 操作按钮
  renderButton() {
    const { loading } = this.props;
    return (
      <>
        <Button
          type="primary"
          icon="plus"
          onClick={() => this.handlAddClick('create')}
          loading={loading}
        >
          新增日志
        </Button>
              </>
    );
  }

  // 简单搜索条件
  renderSearchForm() {
    const { form } = this.props;
    const { loading } = this.props.log;

    const formItemLayout = {
      labelCol: { span: 8 },
      wrapperCol: { span: 16 },
    };

    return (
      <Form api={form} {...formItemLayout} colon layout="inline" onSubmit={this.handleSearch}>
        <Input label="日志名称" id="name" />

        <Input label="角色编码" id="code" />

        <Button type="primary" htmlType="submit" loading={loading}>
          查询
        </Button>
        <Divider type="vertical" />
        <Button htmlType="reset" onClick={() => this.handleFormReset()} loading={loading}>
          重置
        </Button>
      </Form>
    );
  }

  render() {
    const { form } = this.props;
    const { modalType, } = this.props.log;
    return (
      <PageHeaderWrapper>
        {/* 工具条 */}
        <div className="eva-ribbon">
          {/* 操作按钮 */}
          <div>{this.renderButton()}</div>
          {/* 查询条件 */}
          <div>{this.renderSearchForm()}</div>
        </div>
        {/* 删除条幅 */}
        <div className="eva-body">
          <List searchForm={form} />
        </div>
        {/* 新增窗口 */}
        {modalType !== '' && <AOEForm />}
      </PageHeaderWrapper>
    );
  }
}
