package org.pkaq.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.sys.user.entity.AccountProfileEntity;

/**
 * 管理档案存储操作。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Mapper
public interface AccountProfileMapper extends BaseMapper<AccountProfileEntity> {
}
