package io.nerv.core.util;

import cn.hutool.core.collection.CollUtil;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 集合工具类，用于执行常见的集合操作，例如将列表转换为映射，或者将集合转换为不同的类型。
 */
public class GatherUtil {

    /**
     * 将一个集合转换为一个映射，其中映射的键是通过指定的函数从集合元素中派生的。
     *
     * @param from    要转换的源集合。如果集合为空或为null，则返回一个空的映射。
     * @param keyFunc 提取每个集合元素的键的函数。
     * @param <T>     集合中元素的类型。
     * @param <K>     映射中键的类型。
     * @return 返回一个映射，其中键是通过给定的函数从集合元素中提取的，值为集合中的元素本身。
     */
    public static <T, K> Map<K, T> convertMap(Collection<T> from, Function<T, K> keyFunc) {
        if (CollUtil.isEmpty(from)) {
            return new HashMap<>();
        }
        return convertMap(from, keyFunc, Function.identity());
    }

    /**
     * 将一个集合转换为一个映射，其中映射的键和值分别通过指定的函数从集合元素中派生。
     *
     * @param from      要转换的源集合。如果集合为空或为null，则返回一个空的映射。
     * @param keyFunc   提取每个集合元素的键的函数。
     * @param valueFunc 提取每个集合元素的值的函数。
     * @param <T>       集合中元素的类型。
     * @param <K>       映射中键的类型。
     * @param <V>       映射中值的类型。
     * @return 返回一个映射，其中键和值通过给定的函数从集合元素中提取。
     */
    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        if (CollUtil.isEmpty(from)) {
            return new HashMap<>();
        }
        return convertMap(from, keyFunc, valueFunc, (v1, v2) -> v1);
    }

    /**
     * 将一个集合转换为一个映射，其中映射的键和值通过指定的函数从集合元素中派生。
     * 当遇到键冲突时，通过指定的合并函数处理。
     *
     * @param from          要转换的源集合。如果集合为空或为null，则返回一个空的映射。
     * @param keyFunc       提取每个集合元素的键的函数。
     * @param valueFunc     提取每个集合元素的值的函数。
     * @param mergeFunction 处理键冲突时合并值的函数。
     * @param <T>           集合中元素的类型。
     * @param <K>           映射中键的类型。
     * @param <V>           映射中值的类型。
     * @return 返回一个映射，其中键和值通过给定的函数从集合元素中提取，并处理键冲突。
     */
    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunction) {
        if (CollUtil.isEmpty(from)) {
            return new HashMap<>();
        }
        return convertMap(from, keyFunc, valueFunc, mergeFunction, HashMap::new);
    }

    /**
     * 将一个集合转换为一个映射，其中映射的键和值通过指定的函数从集合元素中派生。
     * 当遇到键冲突时，通过指定的合并函数处理，并允许通过指定的映射提供者来控制映射的具体实现类型。
     *
     * @param from          要转换的源集合。如果集合为空或为null，则返回一个空的映射。
     * @param keyFunc       提取每个集合元素的键的函数。
     * @param valueFunc     提取每个集合元素的值的函数。
     * @param mergeFunction 处理键冲突时合并值的函数。
     * @param supplier      提供映射实现的供应者（如 HashMap::new）。
     * @param <T>           集合中元素的类型。
     * @param <K>           映射中键的类型。
     * @param <V>           映射中值的类型。
     * @return 返回一个映射，其中键和值通过给定的函数从集合元素中提取，并处理键冲突和映射的实现类型。
     */
    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunction, Supplier<? extends Map<K, V>> supplier) {
        if (CollUtil.isEmpty(from)) {
            return new HashMap<>();
        }
        return from.stream().collect(Collectors.toMap(keyFunc, valueFunc, mergeFunction, supplier));
    }

    /**
     * 将一个集合转换为一个列表，列表的元素通过指定的函数从集合元素中派生。
     *
     * @param from 要转换的源集合。如果集合为空或为null，则返回一个空的列表。
     * @param func 提取每个集合元素的值的函数。
     * @param <T>  集合中元素的类型。
     * @param <U>  列表中元素的类型。
     * @return 返回一个列表，其中的元素通过给定的函数从集合元素中提取，并过滤掉空值。
     */
    public static <T, U> List<U> convertList(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 将一个集合转换为一个集合（Set），集合的元素通过指定的函数从集合元素中派生。
     *
     * @param from 要转换的源集合。如果集合为空或为null，则返回一个空的集合。
     * @param func 提取每个集合元素的值的函数。
     * @param <T>  集合中元素的类型。
     * @param <U>  集合（Set）中元素的类型。
     * @return 返回一个集合，其中的元素通过给定的函数从集合元素中提取，并过滤掉空值。
     */
    public static <T, U> Set<U> convertSet(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new HashSet<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
