> 该代码生成器基于 mybatis-plus-generator 进行了模板重写


## 使用方式

执行`SingleCodeGenerator`类中的`main`方法即可;

**注意事项**

1.使用时需修改`SingleCodeGenerator`类中的常量;   
2.表名可支持多个，多个表之间使用 ， 隔开;   
3.可通过修改`setSuperEntityColumns`来避免重复生成基类中已经提供的属性;   
4.默认启用了swagger和lombok支持;   
5.默认采用`velocity`引擎模板，如需扩展可自行编写`.vm`文件后修改生成器中的模板配置即可；   