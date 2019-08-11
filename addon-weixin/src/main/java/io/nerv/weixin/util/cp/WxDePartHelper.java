package io.nerv.weixin.util.cp;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.WxCpDepart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信部门工具类
 */
@Slf4j
@Component
public class WxDePartHelper {
    @Autowired
    private WxCpService wxCpService;


    /**
     * 同步部门信息
     * @param departId
     * @param departName
     */
    public Long syncDepart(Long parentDepartId, Long departId, String departName) throws WxErrorException {
        if ( null != departId ){
            this.update(departId, departName);
            return departId;
        } else {
            return this.create(departName, parentDepartId);
        }
    }
    /**
     * 判断部门是否存在
     * @param departId
     * @param departName
     * @return
     * @throws WxErrorException
     */
    public WxCpDepart getDepart(Long departId, String departName) throws WxErrorException {

        List<WxCpDepart> departs = this.list(departId)
                                        .stream()
                                        .filter(item ->
                                            item.getName().equals(departName)
                                        )
                                        .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(departs)){
            return departs.get(0);
        } else {
            return null;
        }
    }

    /**
     * 获取所有部门
     * @param id
     * @return
     * @throws WxErrorException
     */
    public List<WxCpDepart> list(Long id) throws WxErrorException {
        return wxCpService.getDepartmentService().list(id);
    }

    /**
     * 创建部门
     * @param name
     * @param parentId
     * @throws WxErrorException
     * @return
     */
    public Long create(String name, Long parentId) throws WxErrorException {
        WxCpDepart depart = new WxCpDepart();
        depart.setName(name);
        depart.setParentId(parentId);

        return wxCpService.getDepartmentService().create(depart);
    }

    /**
     * 修改部门名称
     * @param departId
     * @param departName
     */
    public void update(Long departId, String departName) throws WxErrorException {
        WxCpDepart depart = new WxCpDepart();
        // 部门Id必须有值,不能为空
        depart.setId(departId);

        // 为要修改的字段赋值
        depart.setName(departName);

        this.wxCpService.getDepartmentService().update(depart);
    }

    /**
     * 删除部门
     * @param departId
     * @throws WxErrorException
     */
    public void del(Long departId) throws WxErrorException {
        wxCpService.getDepartmentService().delete(departId);
    }
}
