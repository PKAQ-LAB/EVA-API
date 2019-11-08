import Taro, { Component } from '@tarojs/taro';
import { View } from '@tarojs/components';
import { connect } from '@tarojs/redux';

@connect(({ home, loading }) => ({
  ...home,
  ...loading,
}))
export default class Index extends Component {
  config = {
    navigationBarTitleText: '首页',
  };

  render() {
    return (
      <View >
          Home page
      </View>
    );
  }
}

