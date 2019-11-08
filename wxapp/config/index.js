const path = require('path');

const config = {
  projectName: 'taro-msparis',
  date: '2018-9-27',
  // 设计稿尺寸
  designWidth: 750,
  sourceRoot: 'src',
  outputRoot: 'dist',
  // 通用插件配置
  plugins: {
    babel: {
      sourceMap: true,
      presets: [
        'env'
      ],
      plugins: [
        'transform-class-properties',
        'transform-decorators-legacy',
        'transform-object-rest-spread'
      ]
    },
  },
  // 全局变量设置
  defineConstants: {},
  alias: {
    '@/components': path.resolve(__dirname, '..', 'src/components'),
    '@/utils': path.resolve(__dirname, '..', 'src/utils'),
    '@/images': path.resolve(__dirname, '..', 'src/images'),
    '@/styles': path.resolve(__dirname, '..', 'src/styles'),
    '@/pages': path.resolve(__dirname, '..', 'src/pages'),
    '@/models': path.resolve(__dirname, '..', 'src/models'),
  },
  // 小程序端专用配置
  weapp: {
    module: {
      postcss: {
        autoprefixer: {
          enable: true
        },
        // 小程序端样式引用本地资源内联配置
        url: {
          enable: true,
          limit: 10240
        }
      }
    }
  },
  // H5 端专用配置
  h5: {
    publicPath: '/',
    staticDirectory: 'static',
    module: {
      postcss: {
        autoprefixer: {
          enable: true
        }
      }
    }
  }
}

module.exports = function (merge) {
  if (process.env.NODE_ENV === 'development') {
    return merge({}, config, require('./dev'))
  }
  return merge({}, config, require('./prod'))
}
