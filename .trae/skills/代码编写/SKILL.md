# Java开发规范 Skill

## 使用场景
当用户要求生成Java代码、审查Java代码、创建Java项目结构或进行Java代码重构时，使用此skill确保代码符合规范。

## 核心原则
1. **可读性优先**: 代码应清晰表达意图，避免过度简化
2. **一致性**: 整个项目保持统一的命名和格式规范
3. **可维护性**: 代码结构清晰，职责分明

---

## 1. 命名规范

### 1.1 通用规则
```java
// ❌ 错误示例
int l = 10;           // 禁止使用L/O
String a1 = "test";   // 无意义命名
String sb2 = "data";  // 无意义命名
String yongHuMing;    // 禁止拼音

// ✅ 正确示例
int lineCount = 10;
String userName = "test";
String orderData = "data";
```

**规则清单**:
- ❌ 禁止使用 `L`/`O`/`l`/`o` 作为变量名
- ❌ 禁止单字母变量(除循环变量 `i`, `j`, `k`)
- ❌ 禁止无意义序列命名(`a1`, `sb2`)
- ❌ 禁止拼音命名
- ✅ 使用完整英文单词
- ✅ 使用领域术语
- ✅ 名称长度 < 30字符
- ❌ 避免仅大小写不同的名称
- ❌ 避免下划线(常量除外)

### 1.2 包名
```java
// ✅ 正确
package com.jmsk.order.service;
package com.jmsk.user.controller;

// ❌ 错误
package com.jmsk.Order.Service;  // 不使用大写
```
- 全部小写
- 使用广泛认可的英文

### 1.3 类名
```java
// ✅ 正确示例
public class UserService {}              // 普通类: 大驼峰
public interface IOrderService {}        // 接口: I开头
public abstract class AbstractEntity {}  // 抽象类: Abstract开头
public class OrderNotFoundException extends Exception {}  // 异常: Exception结尾
public enum OrderStatusEnum {}           // 枚举: Enum结尾

// ❌ 错误示例
public class userService {}              // 首字母应大写
public interface OrderService {}         // 接口应以I开头
public class EntityBase {}               // 抽象类应以Abstract开头
```

**命名规则**:
- 普通类: 大驼峰(PascalCase)，名词
- 接口: `I` + 大驼峰，如 `IUserService`
- 抽象类: `Abstract` + 名词
- 异常类: 名词 + `Exception`
- 枚举类: 名词 + `Enum`

### 1.4 方法和变量
```java
// ✅ 正确示例
public void saveUser() {}           // 动词开头
public User getUserById() {}        // get + 名词
private int userCount;              // 小驼峰
private boolean active;             // boolean不以is开头

// ❌ 错误示例
public void User() {}               // 应以动词开头
public void SaveUser() {}           // 首字母应小写
private boolean isActive;           // 禁止is开头
```

**规则**:
- 小驼峰(camelCase)
- 方法名: 动词 + 名词(动宾结构)
- ❌ boolean属性禁止 `is` 开头

### 1.5 常量
```java
// ✅ 正确
public static final String DEFAULT_ENCODING = "UTF-8";
public static final int MAX_COUNT = 100;

// ❌ 错误
public static final String defaultEncoding = "UTF-8";  // 应全大写
```
- 全大写
- 单词间用下划线分隔

### 1.6 业务对象后缀
```java
// ✅ 正确示例
public class UserBo implements Bo {}          // 接收前端参数
public class UserVo implements Vo {}          // 返回前端视图
public class UserEntity extends StdEntity {}  // 数据库实体
public class UserConvert {}                   // 对象转换
public class UserService {}                   // 业务服务
public class UserCtrl extends Ctrl {}         // 控制器
public class UserMapper {}                    // MyBatis Mapper
public class UserRepository {}                // MongoDB Repository
```

**后缀规范**:
| 类型 | 后缀 | 父类/接口 | 说明 |
|------|------|-----------|------|
| Bo | `Bo` | 实现 `Bo` 接口 | 接收前端参数 |
| Vo | `Vo` | 实现 `Vo` 接口 | 返回前端数据 |
| Entity | `Entity` | 继承 `StdEntity` | 数据库实体，添加 `@alias` |
| Convert | `Convert` | - | MapStruct转换器 |
| Service | `Service` | 可继承 `StdService` | 业务逻辑 |
| Controller | `Ctrl` | 继承 `Ctrl` | 请求处理 |
| Mapper | `Mapper` | - | MyBatis-Plus |
| Repository | `Repository` | - | MongoDB操作 |

---

## 2. 代码格式

### 2.1 缩进和空格
```java
// ✅ 正确: 4空格缩进
public class Demo {
    private String name;
    
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}

// ❌ 错误: 使用tab
public class Demo {
	private String name;  // 使用了tab
}
```

**规则**:
- ✅ 4个空格缩进
- ❌ 禁止tab字符
- 逻辑块间空行分隔
- 运算符前后空格

### 2.2 大括号
```java
// ✅ 正确: 大括号必须使用，左括号同行
public void demo() {
    for (int i = 0; i < 100; i++) {
        // code
    }
    
    int j = 0;
    
    if (1 == j) {
        // even empty
    }
}

// ❌ 错误: 缺少大括号
if (condition) doSomething();  // 禁止

// ❌ 错误: 左括号换行
if (condition)
{  // 禁止
}
```

**规则**:
- `if`/`else`/`for`/`while` 必须用大括号
- 左括号 `{` 在关键字同行末尾(前一空格)
- 右括号 `}` 单独一行

### 2.3 行长度
```java
// ✅ 正确: 不超过120字符
public void saveUserData(String name, String email, String phone) {
    // code
}

// ❌ 错误: 超过120字符需要换行
public void saveUserDataWithVeryLongParameterListThatExceedsOneTwentyCharactersWhichIsNotAllowedInOurCodingStandards() {
}
```
- 最大120字符
- ❌ 禁止调整大于120

### 2.4 常量前置比较
```java
// ✅ 正确: 常量在前
if (1 == status) { }
if ("ACTIVE".equals(userStatus)) { }
if (null == object) { }

// ❌ 错误: 变量在前
if (status == 1) { }
if (userStatus.equals("ACTIVE")) { }  // 可能NPE
```

---

## 3. 注释规范

### 3.1 类注释
```java
/**
 * 用户服务类
 * 负责用户相关的业务逻辑处理
 *
 * @author Zhang San
 * @date 2026-03-01
 */
public class UserService {
}
```

### 3.2 方法注释
```java
/**
 * 新增用户数据
 *
 * @param userBo 用户业务对象
 * @return 新增用户的ID
 * @throws UserExistException 用户已存在时抛出
 */
public Long saveUser(UserBo userBo) throws UserExistException {
    // implementation
}

/**
 * 根据ID查询用户
 *
 * @param userId 用户ID
 * @return 用户信息，不存在返回null
 */
public UserVo getUserById(Long userId) {
    // implementation
}
```

### 3.3 注释类型和要求
```java
// JavaDoc: /** */
/**
 * 类、方法、字段的文档注释
 */

// 多行注释: /* */
/*
 * 复杂逻辑的说明
 * 可以多行
 */

// 单行注释: //
// 简单的代码说明
```

**要求**:
- ✅ 所有类必须有JavaDoc(含创建者、日期)
- ✅ 所有public方法必须有JavaDoc
- ✅ 复杂逻辑需要注释说明意图
- ❌ 禁止注释掉的代码(应删除)

---

## 4. 类和方法规范

### 4.1 类的规范
```java
// ✅ 正确: 职责单一，使用final
public class OrderService {
    private final OrderMapper orderMapper;
    private final UserService userService;
    
    public OrderService(OrderMapper orderMapper, UserService userService) {
        this.orderMapper = orderMapper;
        this.userService = userService;
    }
    
    // 方法不超过200行
    public void createOrder(OrderBo orderBo) {
        // 单一职责: 只负责创建订单
    }
}

// ❌ 错误: 职责混乱
public class OrderService {
    public void createOrder() { }
    public void sendEmail() { }      // 应该在EmailService
    public void updateInventory() { } // 应该在InventoryService
}
```

**规则清单**:
- 类大小 ≤ 1000行(建议≤800)
- 方法大小 ≤ 200行
- 不变变量用 `final` 修饰
- 静态成员用类名调用
- 方法粒度小，单一职责
- ✅ 覆写方法用 `@Override`
- ❌ 禁止过时API
- 使用 `@Deprecated` 需配 `@see`

### 4.2 接口规范
```java
// ✅ 正确
public interface IUserService {
    /**
     * 保存用户
     */
    void saveUser(UserBo userBo);
    
    /**
     * 查询用户
     */
    UserVo getUser(Long id);
    
    // JDK8+ 可以定义default方法
    default void logOperation(String operation) {
        System.out.println("Operation: " + operation);
    }
}

// ❌ 错误
public interface IUserService {
    public void saveUser(UserBo userBo);  // 不需要public
    String USER_TYPE = "NORMAL";          // 不要定义变量
}
```

**规则**:
- ❌ 不加修饰符(public自动添加)
- ✅ 必须有JavaDoc
- ❌ 禁止定义变量
- ✅ 可定义default方法(JDK8+)

---

## 5. 文件组织规范

### 5.1 成员顺序
```java
public class UserService {
    // 1. 静态常量
    private static final String DEFAULT_ROLE = "USER";
    
    // 2. 成员变量
    private final UserMapper userMapper;
    private final Logger logger;
    
    // 3. 构造函数
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.logger = LoggerFactory.getLogger(UserService.class);
    }
    
    // 4. 公共方法
    public void saveUser(UserBo userBo) { }
    
    // 5. 重载方法(紧邻)
    public void saveUser(UserBo userBo, boolean notify) { }
    
    // 6. 私有方法
    private void validateUser(UserBo userBo) { }
}
```

**顺序规则**:
1. 静态常量
2. 成员变量(类头部)
3. 构造函数
4. 公共方法
5. 重载方法(必须相邻)
6. 私有方法(末尾或提取到util)

### 5.2 导入规范
```java
// ✅ 正确: 具体导入
import java.util.List;
import java.util.ArrayList;
import com.jmsk.user.entity.UserEntity;
import com.jmsk.user.vo.UserVo;

// ❌ 错误: 通配符导入
import java.util.*;           // 禁止
import com.jmsk.user.*;      // 禁止

// ❌ 错误: 未使用的导入
import java.util.HashMap;    // 如果代码中未使用
```

**规则**:
- ❌ 禁止通配符 `*` 导入
- ❌ 禁止未使用的import
- ✅ 保存前格式化
- ✅ 保存前优化导入

### 5.3 文件编码
```properties
# .editorconfig 或 IDE设置
charset = utf-8
end_of_line = lf  # Unix格式
```
- ✅ UTF-8编码
- ✅ Unix换行符(LF)
- ❌ 禁止Windows换行符(CRLF)

---

## 6. 数据传输对象(DTO)

### 6.1 Bo示例(接收前端参数)
```java
import org.pkaq.core.mvc.bo.Bo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户业务对象
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Data
@ApiModel("用户业务对象")
public class UserBo implements Bo {
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("电话")
    private String phone;
}
```

### 6.2 Vo示例(返回前端数据)
```java
import org.pkaq.core.mvc.vo.Vo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户视图对象
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Data
@ApiModel("用户视图对象")
public class UserVo implements Vo {
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("邮箱")
    private String email;
}
```

### 6.3 Entity示例(数据库实体)
```java
import org.pkaq.core.mvc.entity.StdEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 用户实体
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@Alias("UserEntity")  // 必须添加别名
public class UserEntity extends StdEntity {
    
    // 禁止添加Swagger注解
    private String username;
    private String email;
    private String phone;
    private String status;
}
```

### 6.4 Convert示例(对象转换)
```java
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户对象转换器
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Mapper
public interface UserConvert {
    
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
    
    /**
     * Bo转Entity
     */
    UserEntity boToEntity(UserBo userBo);
    
    /**
     * Entity转Vo
     */
    UserVo entityToVo(UserEntity userEntity);
}
```

**DTO规则总结**:
- Bo: 实现 `Bo` 接口，可加Swagger注解
- Vo: 实现 `Vo` 接口，可加Swagger注解
- Entity: 继承 `StdEntity`，❌禁止Swagger注解，必须 `@Alias`
- ❌ 禁止直接暴露Entity给前端
- ✅ 使用MapStruct的Convert转换

---

## 7. RESTful API规范

### 7.1 Controller示例
```java
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@RestController
@RequestMapping("/user")
public class UserCtrl extends Ctrl {
    
    private final UserService userService;
    
    public UserCtrl(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Response<Long> createUser(@RequestBody UserBo userBo) {
        Long userId = userService.saveUser(userBo);
        return Response.success(userId);
    }
    
    /**
     * 查询用户详情
     */
    @GetMapping("/{userId}")
    public Response<UserVo> getUser(@PathVariable Long userId) {
        UserVo userVo = userService.getUserById(userId);
        return Response.success(userVo);
    }
    
    /**
     * 查询组织下的用户列表
     */
    @GetMapping("/organization/{organizationId}/employees/list")
    public Response<List<UserVo>> getEmployees(@PathVariable Long organizationId) {
        List<UserVo> employees = userService.getEmployeesByOrg(organizationId);
        return Response.success(employees);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Response<Void> updateUser(@PathVariable Long userId, 
                                      @RequestBody UserBo userBo) {
        userService.updateUser(userId, userBo);
        return Response.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Response<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Response.success();
    }
}
```

### 7.2 API命名规则
```java
// ✅ 正确: URI使用名词，复数用/list
POST   /user                                    // 创建用户
GET    /user/{userId}                           // 查询单个
GET    /user/list                               // 查询列表
PUT    /user/{userId}                           // 更新
DELETE /user/{userId}                           // 删除
GET    /organization/{orgId}/employees/list     // 层级关系

// ❌ 错误: 使用动词
POST   /createUser      // 应使用 POST /user
GET    /getUser/{id}    // 应使用 GET /user/{id}
```

### 7.3 参数传递规则
```java
// POST/PUT/DELETE: JSON Body
@PostMapping("/user")
public Response<Long> createUser(@RequestBody UserBo userBo) { }

// GET: 路径参数 + URL参数，禁止Body
@GetMapping("/user/{userId}")
public Response<UserVo> getUser(@PathVariable Long userId,
                                 @RequestParam(required = false) String detail) { }

// GET涉及敏感字段: 改用POST
@PostMapping("/user/search")  // 原本应该是GET
public Response<List<UserVo>> searchUser(@RequestBody UserSearchBo searchBo) { }
```

**规则**:
- POST/PUT/DELETE: HttpBody传JSON
- GET: 路径参数 + URL参数，❌禁止Body
- 敏感字段: GET改POST，敏感数据放Body
- Content-Type: `application/json`

### 7.4 统一返回格式
```java
import org.pkaq.core.mvc.vo.Response;

// 成功返回
Response.success(data);
Response.success();  // 无数据

// 失败返回
Response.fail(errorCode, errorMessage);

// Response结构
{
    "success": true,
    "code": "0000",
    "message": "操作成功",
    "data": { }
}
```

---

## 8. 数据库规范

### 8.1 表命名
```sql
-- ✅ 正确: 关系表
CREATE TABLE PC_SCHEMA_PKG_REF (
    -- 格式: MCODE_A_B_REF
    -- MCODE: 模块代码
    -- A: 表A描述
    -- B: 表B描述
    -- REF: 关系表标识
);

-- 示例
CREATE TABLE SC_ORDER_PRODUCT_REF;  -- 订单-产品关系表
CREATE TABLE HR_EMPLOYEE_DEPT_REF;  -- 员工-部门关系表
```

### 8.2 索引命名
```sql
-- ✅ 正确: IDX_MCODE_COLUMNNAME
CREATE INDEX IDX_USER_USERNAME ON t_user(username);
CREATE INDEX IDX_ORDER_CREATETIME ON t_order(create_time);

-- ❌ 错误
CREATE INDEX user_name_idx ON t_user(username);  // 格式不对
```

### 8.3 字典字段
```sql
-- 字典类型字段统一使用 varchar(4)
CREATE TABLE t_user (
    user_id BIGINT PRIMARY KEY,
    status VARCHAR(4),      -- 字典字段
    user_type VARCHAR(4)    -- 字典字段
);
```

---

## 9. 异常和国际化

### 9.1 业务异常枚举
```java
import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * 用户模块错误码
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Getter
public enum UserCode implements BizAssert {
    
    /**
     * 用户相关错误 0x75736572-0001
     */
    USER_NOT_FOUND("用户不存在[{0}]", "0x75736572-0001"),
    USER_ALREADY_EXISTS("用户已存在", "0x75736572-0002"),
    PASSWORD_ERROR("密码错误", "0x75736572-0003"),
    USER_DISABLED("用户已禁用", "0x75736572-0004"),
    
    // 通用操作
    OPERATE_SUCCESS("操作成功", "0000"),
    SAVE_SUCCESS("保存成功", "0001");
    
    private final String msg;
    private final String code;
    private final String prefix = "user";
    
    UserCode(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
```

### 9.2 异常使用
```java
public class UserService {
    
    public UserVo getUserById(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        
        // 抛出业务异常
        if (user == null) {
            log.warn("用户不存在: {}", userId);
            UserCode.USER_NOT_FOUND.newException(userId);
        }
        
        return UserConvert.INSTANCE.entityToVo(user);
    }
    
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            log.warn("密码为空");
            UserCode.PASSWORD_ERROR.newException();
        }
    }
}
```

### 9.3 国际化配置
```yaml
# application.yml
spring:
  messages:
    basename: i18n/i18n,i18n/i18n_user,i18n/validate
    encoding: UTF-8
```

```properties
# i18n/i18n_user.properties
0x75736572-0001=User not found [{0}]
0x75736572-0002=User already exists
0x75736572-0003=Password error

# i18n/i18n_user_zh_CN.properties
0x75736572-0001=用户不存在[{0}]
0x75736572-0002=用户已存在
0x75736572-0003=密码错误
```

**规则**:
- 枚举命名: `{Domain}Code`
- 实现 `BizAssert` 接口
- code作为国际化key
- 支持参数占位符 `{0}`, `{1}`

---

## 10. CRUD完整示例

### 10.1 包结构
```
com.jmsk.user
├── controller
│   └── UserCtrl.java
├── service
│   └── UserService.java
├── mapper
│   └── UserMapper.java
├── entity
│   └── UserEntity.java
├── bo
│   └── UserBo.java
├── vo
│   └── UserVo.java
├── convert
│   └── UserConvert.java
└── codes
    └── UserCode.java
```

### 10.2 Controller层
```java
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 用户控制器
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserCtrl extends Ctrl {
    
    private final UserService userService;
    
    @PostMapping
    public Response<Long> create(@RequestBody UserBo userBo) {
        return Response.success(userService.save(userBo));
    }
    
    @GetMapping("/{userId}")
    public Response<UserVo> get(@PathVariable Long userId) {
        return Response.success(userService.getById(userId));
    }
    
    @GetMapping("/list")
    public Response<List<UserVo>> list(@RequestParam(required = false) String username) {
        return Response.success(userService.list(username));
    }
    
    @PutMapping("/{userId}")
    public Response<Void> update(@PathVariable Long userId, 
                                  @RequestBody UserBo userBo) {
        userService.update(userId, userBo);
        return Response.success();
    }
    
    @DeleteMapping("/{userId}")
    public Response<Void> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return Response.success();
    }
}
```

### 10.3 Service层
```java
import org.pkaq.core.mvc.service.StdService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends StdService {
    
    private final UserMapper userMapper;
    
    /**
     * 保存用户
     *
     * @param userBo 用户业务对象
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(UserBo userBo) {
        // 验证用户名是否存在
        UserEntity existing = userMapper.selectByUsername(userBo.getUsername());
        if (null != existing) {
            log.warn("用户已存在: {}", userBo.getUsername());
            UserCode.USER_ALREADY_EXISTS.newException();
        }
        
        // 转换并保存
        UserEntity entity = UserConvert.INSTANCE.boToEntity(userBo);
        userMapper.insert(entity);
        
        log.info("用户创建成功: {}", entity.getUserId());
        return entity.getUserId();
    }
    
    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户视图对象
     */
    public UserVo getById(Long userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (null == entity) {
            log.warn("用户不存在: {}", userId);
            UserCode.USER_NOT_FOUND.newException(userId);
        }
        
        return UserConvert.INSTANCE.entityToVo(entity);
    }
    
    /**
     * 查询用户列表
     *
     * @param username 用户名(可选)
     * @return 用户列表
     */
    public List<UserVo> list(String username) {
        List<UserEntity> entities = userMapper.selectList(username);
        return entities.stream()
                       .map(UserConvert.INSTANCE::entityToVo)
                       .collect(Collectors.toList());
    }
    
    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param userBo 用户业务对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, UserBo userBo) {
        UserEntity entity = userMapper.selectById(userId);
        if (null == entity) {
            UserCode.USER_NOT_FOUND.newException(userId);
        }
        
        // 更新字段
        entity.setEmail(userBo.getEmail());
        entity.setPhone(userBo.getPhone());
        
        userMapper.updateById(entity);
        log.info("用户更新成功: {}", userId);
    }
    
    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        int count = userMapper.deleteById(userId);
        if (0 == count) {
            UserCode.USER_NOT_FOUND.newException(userId);
        }
        
        log.info("用户删除成功: {}", userId);
    }
}
```

### 10.4 Mapper层
```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author Zhang San
 * @date 2026-03-01
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    
    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity selectByUsername(@Param("username") String username);
    
    /**
     * 查询用户列表
     *
     * @param username 用户名(可选)
     * @return 用户列表
     */
    List<UserEntity> selectList(@Param("username") String username);
}
```

---

## 11. 代码生成检查清单

在生成Java代码时，请确保:

### 命名检查
- [ ] 包名全小写
- [ ] 类名大驼峰，接口I开头，抽象类Abstract开头
- [ ] 方法和变量小驼峰
- [ ] 常量全大写下划线分隔
- [ ] Bo/Vo/Entity等后缀正确
- [ ] boolean属性不以is开头
- [ ] 无单字母变量(除i,j,k)

### 格式检查
- [ ] 4空格缩进(不用tab)
- [ ] 大括号必须使用，左括号同行
- [ ] 行长度≤120字符
- [ ] 常量前置比较
- [ ] 逻辑块间有空行

### 注释检查
- [ ] 类有JavaDoc(含作者、日期)
- [ ] 公共方法有JavaDoc
- [ ] 参数和返回值有说明
- [ ] 无注释掉的代码

### 结构检查
- [ ] 成员变量在类头部
- [ ] 构造函数在变量后
- [ ] 重载方法相邻
- [ ] 无通配符import
- [ ] 无未使用import
- [ ] 不变变量用final

### 业务检查
- [ ] Entity继承StdEntity并添加@Alias
- [ ] Bo实现Bo接口
- [ ] Vo实现Vo接口
- [ ] Controller继承Ctrl
- [ ] Service可继承StdService
- [ ] 异常枚举实现BizAssert
- [ ] 返回统一使用Response

---

## 12. 常见错误和修正

### 错误1: boolean属性以is开头
```java
// ❌ 错误
private boolean isActive;

// ✅ 正确
private boolean active;
```

### 错误2: Entity暴露给前端
```java
// ❌ 错误
@GetMapping("/user/{id}")
public Response<UserEntity> getUser(@PathVariable Long id) {
    return Response.success(userMapper.selectById(id));
}

// ✅ 正确
@GetMapping("/user/{id}")
public Response<UserVo> getUser(@PathVariable Long id) {
    UserEntity entity = userMapper.selectById(id);
    return Response.success(UserConvert.INSTANCE.entityToVo(entity));
}
```

### 错误3: 缺少大括号
```java
// ❌ 错误
if (condition) doSomething();

// ✅ 正确
if (condition) {
    doSomething();
}
```

### 错误4: 使用通配符导入
```java
// ❌ 错误
import java.util.*;

// ✅ 正确
import java.util.List;
import java.util.ArrayList;
```

### 错误5: 变量在前比较
```java
// ❌ 错误
if (status == 1) { }
if (name.equals("admin")) { }  // 可能NPE

// ✅ 正确
if (1 == status) { }
if ("admin".equals(name)) { }
```

---

## 总结

使用此skill生成Java代码时，请:
1. 严格遵守命名规范(包、类、方法、变量)
2. 保持代码格式一致(缩进、括号、行长)
3. 添加完整JavaDoc注释
4. 正确使用Bo/Vo/Entity分层
5. 统一异常处理和国际化
6. 遵循RESTful API规范
7. 保持代码职责单一、结构清晰
