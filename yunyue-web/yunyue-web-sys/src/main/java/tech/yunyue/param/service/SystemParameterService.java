package tech.yunyue.param.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.yunyue.param.bo.SystemParameterEditBo;
import tech.yunyue.param.entity.SystemParameterEntity;
import tech.yunyue.param.mapper.SystemParameterMapper;
import tech.yunyue.param.vo.SystemParameterDetailVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : dmz
 */
@Service
@RequiredArgsConstructor
public class SystemParameterService {

    /**
     * 系统参数设置Mapper
     */
    private final SystemParameterMapper systemParameterMapper;

    /**
     * 查询系统参数
     */
    public List<SystemParameterDetailVo> get() {
        LambdaQueryWrapper<SystemParameterEntity> wrapper = Wrappers.lambdaQuery();
        List<SystemParameterEntity> list = systemParameterMapper.selectList(wrapper);
        return list.stream().map(item -> {
            SystemParameterDetailVo vo = new SystemParameterDetailVo();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 编辑
     *
     * @param systemParameterEditBo 实体参数
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(List<SystemParameterEditBo> systemParameterEditBo) {

        systemParameterEditBo.forEach(item -> {
            SystemParameterEntity dto = new SystemParameterEntity();
            BeanUtils.copyProperties(item, dto);
            this.systemParameterMapper.updateById(dto);
        });

    }


}
