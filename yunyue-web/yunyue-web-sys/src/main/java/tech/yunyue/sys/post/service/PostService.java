package tech.yunyue.sys.post.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEnum;
import tech.yunyue.sys.module.entity.ModuleEntityStd;
import tech.yunyue.sys.post.bo.PostEditBo;
import tech.yunyue.sys.post.bo.PostQueryBo;
import tech.yunyue.sys.post.consts.SYSConstant;
import tech.yunyue.sys.post.entity.PostEntity;
import tech.yunyue.sys.post.errorcode.SYSCode;
import tech.yunyue.sys.post.mapper.PostMapper;
import tech.yunyue.sys.post.vo.PostDetailVo;
import tech.yunyue.sys.post.vo.PostTableVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dmz
 */
@Service
@RequiredArgsConstructor
@Schema(description = "岗位管理")
public class PostService {

    /**
     * 岗位Mapper
     */
    private final PostMapper postMapper;

    /**
     * 编码/供应商名称 唯一性校验
     */
    public boolean checkUnique(PostEditBo postEditBo) {
        LambdaQueryWrapper<PostEntity> wrapper = Wrappers.lambdaQuery();

        wrapper.ne(StrUtil.isNotBlank(postEditBo.getId()), PostEntity::getId, postEditBo.getId())
                .and(w -> w.eq(PostEntity::getTitle, postEditBo.getTitle())
                        .or()
                        .eq(PostEntity::getCode, postEditBo.getCode()));

        Long results = this.postMapper.selectCount(wrapper);

        return results < 1;
    }

    /**
     * 分页列表查询
     *
     * @param query 分页参数
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "分页查询岗位")
    public List<Tree<String>> list(PostQueryBo query) {

        List<PostTableVo> listVo = this.postMapper.list(SYSConstant.COMMON_STATUS_DICT, query);
        // 配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 ，即返回列表里对象的字段名
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("parentId");
        treeNodeConfig.setChildrenKey("children");
        treeNodeConfig.setNameKey("title");
        // 使用Hutool自带树状工具方法将列表数据转化成树状结构
        return TreeUtil.build(listVo, SYSConstant.ROOT, treeNodeConfig,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getParentId());
                    tree.setName(treeNode.getTitle());
                    tree.putExtra("code", treeNode.getCode());
                    tree.putExtra("level", treeNode.getLevel());
                    tree.putExtra("sorts", treeNode.getSorts());
                    tree.putExtra("status", treeNode.getStatus());
                    tree.putExtra("parentName", treeNode.getParentName());
                });
    }

    /**
     * 新增/编辑
     *
     * @param postEditBo 实体参数
     */
    @BizLog(operateType = BizLogEnum.CREATE_UPDATE, description = "保存岗位[{0}]", args = {"param:0.id"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(PostEditBo postEditBo) {

        boolean isUpdate = StrUtil.isNotBlank(postEditBo.getId());

        PostEntity dto = new PostEntity();
        if(isUpdate){
            dto = this.postMapper.selectById(postEditBo.getId());
        }

        BeanUtils.copyProperties(postEditBo, dto);
        // 存入path等级祖籍
        if (!StrUtil.equals(SYSConstant.ROOT, postEditBo.getParentId()) && StrUtil.isNotBlank(postEditBo.getParentId())) {
            PostEntity parent = this.postMapper.selectById(postEditBo.getParentId());
            if (null != parent && StringUtils.isNotEmpty(parent.getPathId())) {
                dto.setPathId(parent.getPathId() + StrUtil.COMMA + postEditBo.getParentId());
            } else {
                dto.setPathId(postEditBo.getParentId());
            }
        } else {
            dto.setParentId(SYSConstant.ROOT);
        }
        // 检测通过 保存
        if (isUpdate) {
            this.postMapper.updateById(dto);
        } else {
            this.postMapper.insert(dto);
        }
    }

    /**
     * 根据id查询详情
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据id查询岗位")
    public PostDetailVo get(String id) {

        PostDetailVo vo = new PostDetailVo();
        PostEntity entity = this.postMapper.selectById(id);
        if (ObjectUtil.isNull(entity)) {
            SYSCode.RECORD_NOT_FOUND.newException();
        }
        BeanUtils.copyProperties(entity, vo);

        if (!SYSConstant.ROOT.equals(entity.getParentId())) {
            PostEntity parentEntity = this.postMapper.selectById(entity.getParentId());
            if (ObjectUtil.isNotNull(parentEntity)) {
                vo.setParentName(parentEntity.getTitle());
            }
        }
        return vo;
    }

    /**
     * 批量删除记录
     *
     * @param param 批量传入id
     */
    @BizLog(operateType = BizLogEnum.DELETE, description = "删除岗位[{0}]", args = {"param:0"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void del(ArrayList<String> param) {
        // 限制： 最多只允许同时删除100条
        if (param.size() > 100) {
            SYSCode.DELETE_LIMIT.newException();
        }
        // 若父节点还有子节点则不能删除
        LambdaQueryWrapper<PostEntity> oew = new LambdaQueryWrapper<>();
        oew.in(PostEntity::getParentId, param);
        List<PostEntity> list = this.postMapper.selectList(oew);
        if(CollectionUtil.isNotEmpty(list)){
            SYSCode.DELETE_EXISTENCE_CHILD_NODE.newException();
        }

        // 查询所删除ID
        LambdaQueryWrapper<PostEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(PostEntity::getId, param);

        this.postMapper.delete(lambdaQueryWrapper);
    }

}
