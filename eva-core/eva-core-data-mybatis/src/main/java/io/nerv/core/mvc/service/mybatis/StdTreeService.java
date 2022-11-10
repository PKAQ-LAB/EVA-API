package io.nerv.core.mvc.service.mybatis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.entity.mybatis.StdTreeEntity;
import io.nerv.core.mvc.mapper.StdTreeMapper;
import io.nerv.core.mvc.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * @author S.PKAQ
 */
public abstract class StdTreeService<M extends StdTreeMapper<T>, T extends StdTreeEntity> {
    @Autowired
    public M mapper;

    /**
     * 查询结构树
     * @return
     */
    public List<T> lisTree(T entity){
        return this.mapper.listTree(entity);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity){
        if (entity.getId() == null){
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
    }
    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public Response delete(ArrayList<String> ids){
        Response response = null;
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<T> oew = new QueryWrapper<>();
        oew.in("PARENT_ID", ids);

        List<T> leafList = this.mapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)){
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            String name = CollectionUtil.join(list, ",");
            response = new Response().failure(BizCodeEnum.CHILD_EXIST,name);
        } else {
            this.mapper.deleteBatchIds(ids);
            response = new Response().success();
        }

        return response;
    }

    /**
     * 新增/编辑一条信息
     * @param entity 要 新增/编辑 的对象
     */
    public void edit(T entity){
        String orgId = entity.getId();
        // 获取上级节点
        String pid = entity.getParentId();
        String root = "0";
        if(!root.equals(pid) && StrUtil.isNotBlank(pid)){
            // 查询新父节点信息
            T parent = this.get(pid);
            // 设置当前节点信息
            String parentPath = StrUtil.isNotBlank(entity.getId()) ? entity.getPath()+"/"+entity.getId() : parent.getPath();
            entity.setPath(parentPath);
            String pathName = parent.getPathName()+"/"+entity.getName();
            entity.setPathName(pathName);
            entity.setParentName(parent.getName());

        } else {
            // 父节点为空, 根节点 设置为非叶子\
            pid = root;
            if(StrUtil.isNotBlank(entity.getId())){
                entity.setPath(entity.getId());
            }
            entity.setParentId(pid);
            entity.setIsleaf(false);
            entity.setPathName(entity.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        T orginNode = this.mapper.getParentById(orgId);

        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if(null != orginNode && !pid.equals(orginNode.getParentId())){
            int brothers = this.mapper.countPrantLeaf(orgId) - 1;
            if(brothers < 1){
                orginNode.setIsleaf(true);
                this.update(orginNode);
            }
        }
        //如果是新增且orders属性为空则设置orders属性
        T oldOrgin = null;
        if (StrUtil.isBlank(entity.getId()) ) {
            QueryWrapper<T> orderQuery = new QueryWrapper<>();
            orderQuery.eq("PARENT_ID",pid);
            orderQuery.eq("DELETED", CommonConstant.EFFECTIVE_RECORD);

            entity.setOrders(this.mapper.selectCount(orderQuery));
        } else {
            oldOrgin = this.mapper.selectById(orgId);
        }
        this.merge(entity);

        //新增
        if(null == oldOrgin) {
            //设置path路径 把path路径加上自己本身
            //String path= StrUtil.isBlank(organization.getPath()) ? organization.getId() : organization.getPath() + "/" + organization.getId();
            this.mapper.updateById(entity);
        } else {
            //刷新子节点相关数据
            this.refreshChild(entity, oldOrgin);
        }
        // 保存完重新查询一遍列表数据
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(T entity,T origin){
        // 刷新子节点名称
        this.mapper.updateChildParentName(entity.getName(), entity.getId());
        // TODO 刷新所有子节点的 path_name 和 path
        this.mapper.updateChildPathInfo(entity,origin);
    }
    /**
     * 根据ID更新
     * @param entity
     */
    public void update(T entity){
        // 检查是否存在叶子节点，存在 返回叶子节点名称 终止删除
        this.mapper.updateById(entity);
    }

    /**
     * 根据ID获取一条
     * @param id ID
     * @return
     */
    public T get(String id) {
        return this.mapper.selectById(id);
    }

    /**
     *  交换两个orders值
     * @param entity 进行交换的两个实体
     */
    public void swtich(T[] entity) {
        for (T org : entity) {
            this.mapper.updateById(org);
        }
    }

    /**
     * 切换可用状态 - 级联操作
     * @param entity
     */
    public void changeStatus(T entity) {
        this.mapper.changeStatus(entity);
    }

    /**
     * 查询符合条件的结果条数
     * @param entity
     * @return
     */
    public Long count(T entity) {
        return this.mapper.selectCount(new QueryWrapper<>(entity));
    }
}
