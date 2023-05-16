package tech.yunyue.config;


import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * 用户权限和路由匹配的策略 目前的匹配策略可以使用 暂不需要自定义
 */
public class RouterAuthStrategy implements BiFunction<List<String>, String, Boolean> {
    @Override
    public Boolean apply(List<String> list, String element) {

        // 空集合直接返回false
        if(list == null || list.size() == 0) {
            return false;
        }

        // 先尝试一下简单匹配，如果可以匹配成功则无需继续模糊匹配
        if (list.contains(element)) {
            return true;
        }

        // 开始模糊匹配
        for (String patt : list) {
            if(vagueMatch(patt, element)) {
                return true;
            }
        }

        // 走出for循环说明没有一个元素可以匹配成功
        return false;
    }

    private boolean vagueMatch(String patt, String str) {
        // 两者均为 null 时，直接返回 true
        if(patt == null && str == null) {
            return true;
        }
        // 两者其一为 null 时，直接返回 false
        if(patt == null || str == null) {
            return false;
        }
        // 如果表达式不带有*号，则只需简单equals即可 (这样可以使速度提升200倍左右)
        if(patt.indexOf("*") == -1) {
            return patt.equals(str);
        }
        // 正则匹配
        return Pattern.matches(patt.replaceAll("\\*", ".*"), str);
    }
}
