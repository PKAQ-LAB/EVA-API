package org.pkaq.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;

/**
 * 字符串工具类
 *
 * @author xiaoleilu
 */
public class StrUtil {

    /**
     * <p>指定字符串数组中，是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.hasBlank()                  // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("", null, " ")     // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("123", " ")        // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasBlank(CharSequence...)            等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllBlank()                  // true}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("", null, " ")     // true}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("123", " ")        // false}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasBlank(CharSequence...)}   等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>isAllBlank(CharSequence...)          等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotBlank(str)) {
                return false;
            }
        }
        return true;
    }


    /**
     * <p>字符串是否为空白，空白的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isBlank(null)     // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("")       // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank(" \t\n")  // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢。</p>
     * <br>
     *
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasBlank(CharSequence...)} 或 {@link #isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(CharSequence str) {
        final int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (false == CharUtil.isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     *     <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isNotBlank(null)     // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("")       // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢。</p>
     * <p>建议：仅对于客户端（或第三方接口）传入的参数使用该方法。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }


    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.hasEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "")         // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>字符串是否为空，空的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isEmpty(null)     // true}</li>
     *     <li>{@code CharSequenceUtil.isEmpty("")       // true}</li>
     *     <li>{@code CharSequenceUtil.isEmpty(" \t\n")  // false}</li>
     *     <li>{@code CharSequenceUtil.isEmpty("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议用于工具类或任何可以预期的方法参数的校验中。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasEmpty(CharSequence...)} 或 {@link #isAllEmpty(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 是否为空
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isNotEmpty(null)     // false}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty("")       // false}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：该方法建议用于工具类或任何可以预期的方法参数的校验中。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }


}
