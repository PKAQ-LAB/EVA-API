package io.nerv.core.mvc.service.jpa

import io.nerv.core.constant.CommonConstant
import io.nerv.core.mvc.entity.jpa.StdBaseDomain
import io.nerv.core.mvc.vo.PageVo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * jpa service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * Datetime: 2018/3/13 22:16
 * @author S.PKAQ
 */
abstract class StdBaseService<R : JpaRepository<*, *>?, D : StdBaseDomain?> {
    @JvmField
    @Autowired
    var repository: R? = null

    /**
     * 查询所有记录 - 无分页
     * @return
     */
    fun list(): List<D> {
        return repository!!.findAll()
    }

    /**
     * 根据实体类属性查询所有数据 - 无分页
     * @param domain
     * @return
     */
    fun list(domain: D): List<D> {
        return repository.findAll(Example.of(domain))
    }

    /**
     * 通用根据ID查询
     * @param id id
     * @return 实体类对象
     */
    fun getById(id: String?): Optional<*> {
        return repository!!.findById(id)
    }

    /**
     * 查询符合条件得记录条数
     * @return
     */
    fun selectCount(): Long {
        return repository!!.count()
    }

    /**
     * 查询符合条件得记录条数
     * @param domain
     * @return
     */
    fun selectCount(domain: D): Long {
        return repository!!.count(Example.of(domain))
    }

    /**
     * 根据条件获取一条记录
     * @param domain
     * @return
     */
    fun getOne(domain: D): Optional<*> {
        return repository!!.findOne(Example.of(domain))
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param domain 实体类对象
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    fun merge(domain: D) {
        repository!!.save(domain)
    }

    /**
     * 按分页查询
     * @param domain    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    fun listPage(domain: D?, page: Int?, size: Int?): PageVo<D> {
        var page = page
        var size = size
        page = if (page == null) 0 else page - 1
        size = size ?: CommonConstant.PAGE_SIZE
        val pageable: Pageable = PageRequest.of(page, size, Sort.Direction.DESC, "gmtModify")
        var result: Page<D>? = null
        if (null == domain) {
            result = repository!!.findAll(pageable)
        } else {
            result = repository.findAll(Example.of(domain), pageable)
        }
        return PageVo<D>().setCurrent(result!!.number + 1.toLong())
                .setSize(result!!.size.toLong())
                .setTotal(result.totalElements)
                .setRecords(result.content)
    }
    //
    /**
     * 通用分页查询
     * @param domain    目标实体类
     * @param page      当前页码
     * @return          分页模型类
     */
    fun listPage(domain: D, page: Int?): PageVo<D> {
        var page = page
        page = if (page == null) 0 else page - 1
        val pageable: Pageable = PageRequest.of(page, CommonConstant.PAGE_SIZE, Sort.Direction.ASC, "gmtModify")
        val result: Page<D> = repository.findAll(Example.of(domain), pageable)
        return PageVo<D>().setCurrent(result.number + 1.toLong())
                .setSize(result.size.toLong())
                .setTotal(result.totalElements)
                .setRecords(result.content)
    }

    /**
     * 通用删除
     * @param domains
     * @return
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    open fun delete(domains: List<D>?) {
        repository!!.deleteInBatch(domains)
    }
}