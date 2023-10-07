package tech.yunyue.core.mybatis.log.login.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import tech.yunyue.core.log.base.LoginlogEntity;

/**
 * 登录/登出日志 mybatis实体类
 */
@Data
@Alias("loginlog")
@TableName("log_login")
@EqualsAndHashCode(callSuper = true)
public class MybatisLoginLogEntity extends LoginlogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
}
