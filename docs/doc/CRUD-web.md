主子表的CRUD需要继承 activeRecord系列基类
包括   
- BaseActiveCtrl   
- BaseActiveService
- BaseActiveEntity(主表)   
  BaseLineActiveEntity(子表)
  
  在`service`层调用`megre(mainObj, List<lineObj>)`方法即可   