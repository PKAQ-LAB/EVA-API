import Taro, { Component } from '@tarojs/taro';
import { View } from '@tarojs/components';
import { connect } from '@tarojs/redux';

@connect(({ user, common }) => ({
  ...user,
  ...common,
}))

export default class User extends Component {
  config = {
    navigationBarTitleText: '我的',
  };
  render() {
    return (
      <View>
        Mine mine
      </View>
    );
  }
}
