package org.pkaq.core.auth.spi;

/**
 * 注册账号写入端口，不允许创建管理档案和授权关系。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAccountRegistration {
    /** 创建账号，密码为现有登录契约的MD5表示，由共享创建入口执行单次BCrypt。 */
    Long create(String account, String loginPassword, String nickName);
}
