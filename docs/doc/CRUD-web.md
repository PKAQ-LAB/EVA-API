## 分页
继承`BaseService`后可直接调用如下方法进行分页查询
```java
this.listPage(entity, page);
```
## 单表CRUD
单表的CRUD需要继承 Base系列基类
包括   
- BaseCtrl   
- BaseService
- BaseEntity
  
  在`service`层调用`megre(obj)`方法即可
  
## 主子表CRUD
主子表的CRUD需要继承 activeRecord系列基类
包括   
- BaseActiveCtrl   
- BaseActiveService
- BaseActiveEntity(主表)   
  BaseLineActiveEntity(子表)
  
  在`service`层调用`megre(mainObj, List<lineObj>)`方法即可   
  
  