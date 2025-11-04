package org.pkaq.core.util;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 集合类工具类
 *
 * @author from org.spring
 */
public class CollUtils {
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return CollectionUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    public static <T> List<Object> getFieldValues(Collection<T> list, String fieldName) {
        if (list == null || list.isEmpty()) return List.of();
        List<Object> result = new ArrayList<>();
        for (T item : list) {
            if (item == null) continue;
            try {
                var field = item.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                result.add(field.get(item));
            } catch (Exception e) {
                // 忽略不存在字段
            }
        }
        return result;
    }

    public static String join(Collection<?> coll, String delimiter) {
        if (coll == null || coll.isEmpty()) {
            return "";
        }
        return coll.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }
}
