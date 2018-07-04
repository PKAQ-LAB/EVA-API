PKAQ Design => Ant Design Pro

## 交流QQ群： 83864896

运行前需要安装 
- git
- nodejs
- yarn

需要补充得知识
 - [es6语法](http://es6.ruanyifeng.com)
 - [react入门](http://www.ruanyifeng.com/blog/2015/03/react.html)
 - [react redux入门] (http://www.ruanyifeng.com/blog/2016/09/redux_tutorial_part_one_basic_usages.html)
 - [react router入门] (http://www.ruanyifeng.com/blog/2016/05/react_router.html?utm_source=tool.lu)
 - [saga入门] (https://www.jianshu.com/p/89ed2a01a3db)
---
目录说明   
- dist: 打包目录   
- mock： mock data   
- public： 公共资源   
- src   
  - app 模块目录   
  - assets 资源目录   
  - core 框架核心   
  - component  自定义组件      
   - |- App： 高阶组件      
   - |- Page： 内页包装器       

TIP: - 代码里针对namespace和model文件名进行了忽略大小写的处理
     - reducer名字和effects名字不要重复,否则在调用的时候会出现混乱(优先effects>reducer)

>问题
- 不支model花式命名：当期版本model的文件名必须与所指定的namespace一致，因为router.js中是通过读取文件名来判断已经注册的model
  中是否存在该model的，又由于存在namespace的唯一性约束，所以当两者不一致时无法通过此种方式判断对应文件名的model是否已经注册导致异常
- Mirror Site in China: http://ant-design-pro.gitee.io


## Usage
```bash
$ git clone 
$ yarn install
$ yarn start         # visit http://localhost:8000
```
// 格式化代码
yarn run prettier

![snapshot](snapshot.jpg)
