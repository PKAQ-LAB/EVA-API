package io.nerv.core.mvc.service.mybatis

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.activerecord.Model
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
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
abstract class ActiveBaseService<M : BaseMapper<T>?, T : Model<*>?> {
    @Autowired
    protected var mapper: M? = null

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
     */
    fun selectCount(entity: T) {
        val wrapper: Wrapper<T> = QueryWrapper(entity)
        entity!!.selectCount(wrapper)
    }

    /**
     * 根据条件获取一条记录
     * @param entity
     */
    fun getByEntity(entity: T) {
        val wrapper: Wrapper<T> = QueryWrapper(entity)
        entity!!.selectOne(wrapper)
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param entity 实体类对象
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    fun merge(entity: T) {
        entity!!.insertOrUpdate()
    }

    /**
     * 查询所有
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    protected fun list(entity: T): List<T> {
        val wrapper = QueryWrapper(entity)
        wrapper.orderByDesc("gmt_Modify")
        return entity!!.selectList(wrapper)
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
        return entity!!.selectPage(pagination, wrapper)
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
        return entity!!.selectPage(pagination, wrapper)
    }

    /**
     * 通用删除
     * @param param
     */
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    fun delete(param: ArrayList<String?>?) {
        mapper!!.deleteBatchIds(param)
    }
}