
##防止重复提交  
可在期望的方法上添加如下注解来防止重复提交.   
```java
    @NoRepeatSubmit
```

默认基类中的方法已经添加了此注解

##配置项    
eva.cache.timeout: 重复提交认定间隔，默认3秒