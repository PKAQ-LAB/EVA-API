package org.pkaq.sys.notice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.notice.bo.NoticeAoeBo;
import org.pkaq.sys.notice.bo.NoticeQueryBo;
import org.pkaq.sys.notice.entity.NoticeEntity;
import org.pkaq.sys.notice.mapper.NoticeMapper;
import org.pkaq.sys.notice.vo.NoticeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** 通知服务。 @author PKAQ */
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeMapper noticeMapper;

    /** 获取当前可用通知。 */
    @TenantSchema
    @Transactional(readOnly = true)
    public List<NoticeVo> listAvailable() {
        return this.noticeMapper.selectList(new LambdaQueryWrapper<NoticeEntity>()
                        .eq(NoticeEntity::getFrozen, FrozenEnumm.UN_FROZEN)
                        .orderByDesc(NoticeEntity::getDatetime)
                        .orderByDesc(NoticeEntity::getId))
                .stream().map(this::toVo).toList();
    }

    /** 分页查询通知。 */
    @TenantSchema
    @Transactional(readOnly = true)
    public PageVo<NoticeVo> list(NoticeQueryBo queryBo) {
        NoticeQueryBo query = queryBo == null ? new NoticeQueryBo() : queryBo;
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtils.isNotBlank(query.getTitle()), NoticeEntity::getTitle, query.getTitle());
        wrapper.eq(StrUtils.isNotBlank(query.getType()), NoticeEntity::getType, query.getType());
        wrapper.orderByDesc(NoticeEntity::getDatetime).orderByDesc(NoticeEntity::getId);
        PageResult<NoticeEntity> page = new PageResult<>(query.getPageNo(), query.getPageSize());
        return this.noticeMapper.selectPage(page, wrapper).map(this::toVo);
    }

    /** 根据ID查询通知。 */
    @TenantSchema
    @Transactional(readOnly = true)
    public NoticeVo get(Long id) {
        CommonCodes.NULL_ID.assertNotNull(id);
        NoticeEntity entity = this.noticeMapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return null;
        }
        return toVo(entity);
    }

    /** 新增或编辑通知。 */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class)
    public void edit(NoticeAoeBo bo) {
        if (bo == null || StrUtils.isBlank(bo.getTitle()) || StrUtils.isBlank(bo.getType())) {
            CommonCodes.PARAM_ERROR.newException();
            return;
        }
        NoticeEntity entity = bo.getId() == null || bo.getId() == 0L
                ? new NoticeEntity() : requireEditable(bo.getId());
        entity.setTitle(bo.getTitle().trim());
        entity.setContent(bo.getContent());
        entity.setType(bo.getType().trim());
        entity.setAvatar(bo.getAvatar());
        entity.setDatetime(bo.getDatetime() == null ? LocalDateTime.now() : bo.getDatetime());
        if (bo.getFrozen() != null) {
            entity.setFrozen(resolveFrozen(bo.getFrozen()));
        } else if (entity.getId() == null) {
            entity.setFrozen(FrozenEnumm.UN_FROZEN);
        }
        this.noticeMapper.insertOrUpdate(entity);
    }

    /** 批量删除通知。 */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            CommonCodes.NULL_ID.newException();
            return;
        }
        Long readOnlyCount = this.noticeMapper.selectCount(new LambdaQueryWrapper<NoticeEntity>()
                .in(NoticeEntity::getId, ids)
                .eq(NoticeEntity::getFrozen, FrozenEnumm.READ_ONLY));
        if (readOnlyCount != null && readOnlyCount > 0L) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
        this.noticeMapper.deleteByIds(ids);
    }

    /** 切换通知冻结状态。 */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class)
    public void switchFrozen(SingleArray<Long> ids) {
        if (ids == null || CollUtils.isEmpty(ids.getParam())) {
            CommonCodes.NULL_ID.newException();
            return;
        }
        for (Long id : ids.getParam()) {
            NoticeEntity entity = this.noticeMapper.selectById(id);
            if (entity == null || entity.getFrozen() == FrozenEnumm.READ_ONLY) {
                continue;
            }
            FrozenEnumm target = entity.getFrozen() == FrozenEnumm.FROZEN
                    ? FrozenEnumm.UN_FROZEN : FrozenEnumm.FROZEN;
            this.noticeMapper.update(null, new LambdaUpdateWrapper<NoticeEntity>()
                    .eq(NoticeEntity::getId, id)
                    .set(NoticeEntity::getFrozen, target));
        }
    }

    private NoticeEntity requireEditable(Long id) {
        NoticeEntity entity = this.noticeMapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
            return new NoticeEntity();
        }
        if (entity.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
        return entity;
    }

    private FrozenEnumm resolveFrozen(Integer code) {
        for (FrozenEnumm value : FrozenEnumm.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        CommonCodes.PARAM_ERROR.newException();
        return FrozenEnumm.UN_FROZEN;
    }

    private NoticeVo toVo(NoticeEntity entity) {
        NoticeVo vo = new NoticeVo();
        vo.setId(entity.getId());
        vo.setRevision(entity.getRevision() == null ? 0 : entity.getRevision());
        vo.setFrozen(entity.getFrozen());
        vo.setSort(entity.getSort() == null ? 0D : entity.getSort());
        vo.setCreateId(entity.getCreateId());
        vo.setCreateBy(entity.getCreateBy());
        vo.setUtcCreate(entity.getUtcCreate());
        vo.setModifyId(entity.getModifyId());
        vo.setModifyBy(entity.getModifyBy());
        vo.setUtcModify(entity.getUtcModify());
        vo.setRemark(entity.getRemark());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setType(entity.getType());
        vo.setAvatar(entity.getAvatar());
        vo.setDatetime(entity.getDatetime());
        return vo;
    }
}
