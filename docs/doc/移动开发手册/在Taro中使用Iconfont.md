1. 生成在线代码或直接将代码下载到本地
    p.s ： 如果使用在线代码， 生成的链接为 cdn 链接 需要手工添加 http 前缀
2. 替换下载的iconfont.css 中得图标前缀， 将 icon- 改为 iconfont-
    p.s ： 使用Taro得AtIcon组件时 需要指定 prefixclass，之后生成得默认样式为 prefixclass prefixclass-图标样式名称
           而iconfont得样式规则为 iconfont icon-xxx 所以此处要改
3.  将样式文件引入app.js 即可使用