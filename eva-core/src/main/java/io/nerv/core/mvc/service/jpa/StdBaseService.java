package io.nerv.core.mvc.service.jpa;

import io.nerv.core.constant.PageConstant;
import io.nerv.core.mvc.entity.jpa.StdBaseDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * jpa service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * Datetime: 2018/3/13 22:16
 * @author S.PKAQ
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public abstract class StdBaseService<R extends JpaRepository, D extends StdBaseDomain> {
    @Autowired
    public R repository;
    /**
     * 查询所有记录 - 无分页
     * @return
     */
    public List<D> list(){
        return this.repository.findAll();
    }
    /**
     * 根据实体类属性查询所有数据 - 无分页
     * @param domain
     * @return
     */
    public List<D> list(D domain){
        return this.repository.findAll(Example.of(domain));
    }
    /**
     * 通用根据ID查询
     * @param id id
     * @return 实体类对象
     */
    public Optional getById(String id){
        return this.repository.findById(id);
    }
    /**
     * 查询符合条件得记录条数
     * @return
     */
    public long selectCount(){
        return this.repository.count();
    }
    /**
     * 查询符合条件得记录条数
     * @param domain
     * @return
     */
    public long selectCount(D domain){
        return this.repository.count(Example.of(domain));
    }
    /**
     * 根据条件获取一条记录
     * @param domain
     * @return
     */
    public Optional getOne(D domain){
        return this.repository.findOne(Example.of(domain));
    }
    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param domain 实体类对象
     */
    public void merge(D domain){
        this.repository.save(domain);
    }
    /**
     * 按分页查询
     * @param domain    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    public Page listPage(D domain, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "gmtModify");
        return this.repository.findAll(Example.of(domain), pageable);
    }
//
    /**
     * 通用分页查询
     * @param domain    目标实体类
     * @param page      当前页码
     * @return          分页模型类
     */
    public Page listPage(D domain, Integer page) {
        Pageable pageable = PageRequest.of(page, PageConstant.PAGE_SIZE, Sort.Direction.ASC, "gmtModify");
        return this.repository.findAll(Example.of(domain), pageable);
    }
    /**
     * 通用删除
     * @param domains
     * @return
     */
    public void delete(List<D> domains){
        this.repository.deleteInBatch(domains);
    }
}
