import React from 'react';
import { Divider, Popconfirm } from 'antd';
import { connect } from 'dva';
import DataTable from '@/components/DataTable';

@connect(state => ({
  log: state.log,
  loading: state.loading.effects['log/fetch'],
}))
export default class List extends React.PureComponent {
  // 组件加载完成后加载数据
  componentDidMount() {
    this.props.dispatch({
      type: 'log/fetch',
    });
  }

  // 单条删除
  handleDeleteClick = record => {
    this.props.dispatch({
      type: 'log/remove',
      payload: {
        param: [record.id],
      },
    });
  };

  // 编辑
  handleEditClick = record => {
    this.props.dispatch({
      type: 'log/edit',
      payload: {
        modalType: 'edit',
        id: record.id,
      },
    });
  };

  // 翻页
  handlePageChange = pg => {
    const { dispatch, searchForm } = this.props;
    const { pageNum, pageSize } = pg;

    const params = {
      page: pageNum,
      pageSize,
      ...searchForm.getFieldsValue(),
    };

    dispatch({
      type: 'log/fetch',
      payload: params,
    });
  };

  render() {
    const { loading } = this.props;

    const { roles,  } = this.props.role;

    const columns = [{
        title: '角色名称',
        name: 'name',
        tableItem: {},
      }, {
        title: '角色编码',
        name: 'code',
        tableItem: {},
      }, {
        title: '备注',
        name: 'remark',
      }, {
        tableItem: {
          render: (text, record) =>
            record.status === '0000' && (
              <DataTable.Oper style={{ textAlign: 'center' }}>
                <a onClick={e => this.handleEditClick(record, e)}>编辑</a>
                <Divider type="vertical" />
                <Popconfirm
                  title="确定要删除吗？"
                  okText="确定"
                  cancelText="取消"
                  onConfirm={() => this.handleDeleteClick(record)}
                >
                  <a>删除</a>
                </Popconfirm>
              </DataTable.Oper>
            ),
        },
      },
    ];

    const dataTableProps = {
      columns,
      rowKey: 'id',
      showNum: true,
      loading,
      isScroll: true,
      alternateColor: true,
      dataItems: roles,
      onChange: this.handlePageChange,
    };

    return <DataTable {...dataTableProps} bordered  />;
  }
}
