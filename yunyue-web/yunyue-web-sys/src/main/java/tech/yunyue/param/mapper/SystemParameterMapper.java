package tech.yunyue.param.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.yunyue.param.bo.SystemParameterEditBo;
import tech.yunyue.param.entity.SystemParameterEntity;
import tech.yunyue.param.vo.SystemParameterDetailVo;

import java.util.List;


/**
 * @author : dmz
 */
@Mapper
public interface SystemParameterMapper  extends BaseMapper<SystemParameterEntity> {

    /**
     * 系统参数详情
     *
     * @return 返回系统参数列表对象
     */
    List<SystemParameterDetailVo> detail();
    /**
     * 批量提交审批
     *
     * @param codeVal  参数值
     * @param data     审批数据
     * @param userName 修改人
     */
    void systemParam(@Param("codeVal") String codeVal,
                    @Param("userName") String userName,
                    @Param("data") List<SystemParameterEditBo> data);




}
