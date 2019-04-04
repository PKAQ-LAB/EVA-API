package io.nerv.web.jxc.base.category.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.tree.TreeHelper;
import io.nerv.web.jxc.base.category.entity.CategoryEntity;
import io.nerv.web.jxc.base.category.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Service
public class CategoryService extends BaseService<CategoryMapper, CategoryEntity> {
    /**
     * 查询分类结构树
     * @return
     */
    public List<CategoryEntity> listCategory(String condition, CategoryEntity categoryEntity){
        return this.mapper.listCategory(condition, categoryEntity);
    }

    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public Response deleteCategory(ArrayList<String> ids){
        Response response = new Response();
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<CategoryEntity> oew = new QueryWrapper<>();
        oew.setEntity( new CategoryEntity() );
        oew.in("parent_ID", ids);

        List<CategoryEntity> leafList = this.mapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)){
            // 获取存在子节点的节点名称
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            // 拼接名称
            String name = CollectionUtil.join(list, ",");
            response = response.failure(501, StrUtil.format("[{}] 存在子节点，无法删除。",name), null);
        } else {
            this.mapper.deleteBatchIds(ids);
            response = response.success(this.mapper.listCategory(null, null));
        }

        return response;
    }
    /**
     * 新增/编辑一条分类信息
     * @param category 要 新增/编辑 得分类对象
     * @return 重新查询分类列表
     */
    public List<CategoryEntity> editCategory(CategoryEntity category){
        String categoryId = category.getId();
        // 获取上级节点
        String pid = category.getParentId();
        String root = "0";
        if(!root.equals(pid) && StrUtil.isNotBlank(pid)){
            // 查询新父节点信息
            CategoryEntity parentCategory = this.getCategory(pid);
            // 设置当前节点信息
            String pathName = StrUtil.format("{}/{}", parentCategory.getName(), category.getName());

            String oldFatherPath = null;
            if(categoryId != null ){
                CategoryEntity oldCategory=this.mapper.selectById(categoryId);
                if(oldCategory != null){
                    //得到原来父节点的path路径
                    if(StrUtil.isNotBlank(oldCategory.getParentId())){
                        CategoryEntity oldParent= this.mapper.selectById(oldCategory.getParentId());
                        oldFatherPath=oldParent != null ? oldParent.getPath() : null;
                    }
                }
            }
            category.setPath(TreeHelper.assemblePath(parentCategory.getPath(), category.getPath(),oldFatherPath));
            category.setPathId(parentCategory.getId());
            category.setPathName(pathName);
            category.setParentName(parentCategory.getName());

        } else {
            // 父节点为空, 根节点 设置为非叶子
            category.setIsleaf(false);
            category.setPathId(category.getId());
            category.setParentName(category.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        CategoryEntity categoryinNode = this.mapper.getParentById(categoryId);

        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if(null != categoryinNode && !pid.equals(categoryinNode.getParentId())){
            int brothers = this.mapper.countPrantLeaf(categoryId) - 1;
            if(brothers < 1){
                categoryinNode.setIsleaf(true);
                this.updateCategory(categoryinNode);
            }
            int buddy = this.mapper.countPrantLeaf(pid);
            category.setOrders(buddy);
        }
        this.merge(category);
        // 保存完重新查询一遍列表数据
        return this.listCategory(null, null);
    }

    /**
     * 根据ID更新
     * @param categoryEntity
     * @return
     */
    public void updateCategory(CategoryEntity categoryEntity){
        this.mapper.updateById(categoryEntity);
    }


    /**
     * 根据ID获取一条分类信息
     * @param id 分类ID
     * @return 分类信息
     */
    public CategoryEntity getCategory(String id) {
        return this.getById(id);
    }

    /**
     * 校验同级节点中path是否唯一
     * @param category
     * @return
     */
    public boolean checkUnique(CategoryEntity category) {
        QueryWrapper<CategoryEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("code", category.getCode());

        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }
}
