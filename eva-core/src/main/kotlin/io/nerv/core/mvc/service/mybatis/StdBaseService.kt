package io.nerv.core.mvc.service.mybatis

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity
import io.nerv.core.mvc.util.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * Datetime: 2018/3/13 22:16
 * @author S.PKAQ
 */
abstract class StdBaseService<M : BaseMapper<T>?, T : StdBaseEntity?> {
    @JvmField
    @Autowired
    var mapper: M? = null

    /**
     * 通用根据ID查询
     * @param id id
     * @return 实体类对象
     */
    fun getById(id: String?): T {
        return mapper!!.selectById(id)
    }

    /**
     * 查询符合条件得记录条数
     * @param entity
     * @return
     */
    fun selectCount(entity: T): Int {
        val wrapper: Wrapper<T> = QueryWrapper(entity)
        return mapper!!.selectCount(wrapper)
    }

    /**
     * 根据条件获取一条记录
     * @param entity
     * @return
     */
    fun getByEntity(entity: T): T {
        val wrapper: Wrapper<T> = QueryWrapper(entity)
        return mapper!!.selectOne(wrapper)
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param entity 实体类对象
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    open fun merge(entity: T) {
        if (entity.getId() == null) {
            mapper!!.insert(entity)
        } else {
            mapper!!.updateById(entity)
        }
    }

    /***
     * 根据指定条件合并
     * @param entity
     * @param wrapper
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    open fun merge(entity: T, wrapper: Wrapper<T>?) {
        if (entity.getId() == null) {
            mapper!!.insert(entity)
        } else {
            mapper!!.update(entity, wrapper)
        }
    }

    /**
     * 查询所有
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    open fun list(entity: T): List<T>? {
        val wrapper = QueryWrapper(entity)
        wrapper.orderByDesc("gmt_Modify")
        return mapper!!.selectList(wrapper)
    }

    /**
     * 按分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    fun listPage(entity: T, page: Int?, size: Int?): IPage<T> {
        var page = page
        var size = size
        page = page ?: 1
        size = size ?: 10
        val wrapper = QueryWrapper(entity)
        wrapper.orderByDesc("gmt_Modify")
        val pagination: Page<*> = Page<Any?>()
        pagination.current = page.toLong()
        pagination.size = size.toLong()
        return mapper.selectPage<Page<*>>(pagination, wrapper)
    }

    /**
     * 通用分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @return          分页模型类
     */
    fun listPage(entity: T, page: Int?): IPage<T> {
        var page = page
        page = page ?: 1
        // 查询条件
        val wrapper = QueryWrapper(entity)
        wrapper.orderByDesc("gmt_Modify")
        // 分页条件
        val pagination: Page<*> = Page<Any?>()
        pagination.current = page.toLong()
        return mapper.selectPage<Page<*>>(pagination, wrapper)
    }

    /**
     * 通用删除
     * @param param
     * @return
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    fun delete(param: ArrayList<String?>?) {
        mapper!!.deleteBatchIds(param)
    }
}