package io.nerv.core.advice

import io.nerv.core.annotation.JsonFilter
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.reflect.Field

/**
 * 序列化对象时过滤某些属性
 */
@Aspect
@Component
class JsonFilterAdvice {
    var log = LoggerFactory.getLogger(this.javaClass)
    //切入点
    @Pointcut("@annotation(io.nerv.core.annotation.JsonFilter)")
    fun annotation() {
    }

    /**
     * 以jsonFilter注解为切入点切入方法，序列化对象时过滤某些属性
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("annotation()")
    @Throws(Throwable::class)
    fun doBasicProfiling(pjp: ProceedingJoinPoint): Any? {
        //得到Controller层返回的对象
        val retVal = pjp.proceed()

        //得到方法上的注解
        val clazzz: Class<*> = pjp.target.javaClass
        val mm = clazzz.getDeclaredMethod(pjp.signature.name, pjp.args.javaClass)
        val jsonFilter = mm.getAnnotation(JsonFilter::class.java)
        if (jsonFilter == null || jsonFilter.exclude.size < 1) {
            return null
        } else {
            //操作对象，使得注解里对应的属性值为空
            nullVlueFeild(retVal, jsonFilter.exclude)
        }
        return retVal
    }

    /**
     * 把对象的属性置空
     * @param returnValue
     * @param values
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun nullVlueFeild(returnValue: Any?, values: Array<String>) {
        for (value in values) {
            //根据.号分级
            val splitValue = value.split("\\.".toRegex()).toTypedArray()
            if (splitValue.size == 1) {
                //顶层属性,直接设置属性值为空
                setNull(returnValue, value)
                continue
            }
            //表示上一层的属性对象的值
            var fatherObj = returnValue
            //表示当前循环的属性对象的值
            var `object`: Any
            for (i in 0 until splitValue.size - 1) {

                //得到当前对象的当前属性的值
                val param = splitValue[i]
                var field: Field? = null
                field = try {
                    fatherObj!!.javaClass.getDeclaredField(param)
                } catch (e: NoSuchFieldException) {
                    fatherObj!!.javaClass.superclass.getDeclaredField(param)
                }
                if (field == null) {
                    continue
                }
                field.isAccessible = true
                `object` = field[fatherObj]


                //如果是最后一个对象 即最后一个点之前的属性值 c（a.b.c.d）,直接设置值
                if (splitValue.size - 2 == i) {
                    setNull(`object`, splitValue[i + 1])
                }

                //得到当前循环的子集的所有下层子集
                val begin = value.indexOf(".", i + 1)
                val sValue = arrayOf(value.substring(begin + 1, value.length))

                //判断是否是数组/集合
                if (`object`.javaClass.isArray || `object` is Collection<*>) {
                    for (obj in `object` as Collection<*>) {
                        //把当前属性的对象和所有下层子集传到方法中
                        nullVlueFeild(obj, sValue)
                    }
                    break
                } else if (`object` is Map<*, *>) {
                    //判断是否是map集合
                    //map对象点之后即map的key 第二个点即该key对应的value里的属性
                    var last = sValue[0]
                    last = last.substring(last.indexOf(".") + 1, last.length)
                    sValue[0] = last
                    nullVlueFeild(`object`[splitValue[i + 1]], sValue)
                    break
                }
                //下一级的父对象为当前这一级
                fatherObj = `object`
            }
        }
    }

    //根据对象类型把该对象的value置位空
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setNull(`object`: Any?, value: String?) {
        //判断是否是数组/集合
        if (`object`!!.javaClass.isArray || `object` is Collection<*>) {
            for (obj in (`object` as Collection<*>?)!!) {
                nul(obj, value)
            }
        } else if (`object` is Map<*, *>) {
            //判断是否是map集合

            //判断是否是map集合
            val map = `object` as MutableMap<Any?, Any?>
            map[value] = null
        } else {
            //javabean
            nul(`object`, value)
        }
    }

    //把object的value属性置位空
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun nul(`object`: Any?, value: String?) {
        var field: Field? = null
        for (obj in (`object` as Collection<*>?)!!) {
            field = try {
                obj?.javaClass?.getDeclaredField(value)
            } catch (e: NoSuchFieldException) {
                obj?.javaClass?.superclass?.getDeclaredField(value)
            }
            field!!.isAccessible = true
            field[obj] = null
        }
    }
}
