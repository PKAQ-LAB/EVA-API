package io.nerv.core.mybatis.exception.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * 异常日志实体类
 */
@Data
@Alias("errorlog")
@TableName("log_error")
@Accessors(chain = true)
public class ErrorlogEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 操作人
     **/
    private String requestTime;
    /**
     * 操作人
     **/
    private String ip;
    /**
     * 操作人
     **/
    private String spendTime;
    /**
     * 操作人
     **/
    private String className;
    /**
     * 操作人
     **/
    private String method;
    /**
     * 操作人
     **/
    private String params;
    /**
     * 操作人
     **/
    private String exDesc;
    /**
     * 操作人
     **/
    private String loginUser;
}
