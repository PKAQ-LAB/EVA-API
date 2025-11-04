package org.pkaq.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public final class ExceptionUtils {

    private ExceptionUtils() {}

    /**
     * 将异常堆栈转换为字符串
     */
    public static String stackTraceToString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 将异常堆栈转换为字符串，只包含指定包名前缀的堆栈
     *
     * @param e 异常对象
     * @param packagePrefix 需要保留的包名前缀（如 "com.myapp"）
     * @return 过滤后的堆栈字符串
     */
    public static String stackTraceToString(Throwable e, String packagePrefix) {
        if (e == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");

        Arrays.stream(e.getStackTrace())
                .filter(ste -> ste.getClassName().startsWith(packagePrefix))
                .forEach(ste -> sb.append("\tat ").append(ste).append("\n"));

        // 递归输出 cause
        Throwable cause = e.getCause();
        if (cause != null && cause != e) {
            sb.append("Caused by: ").append(stackTraceToString(cause, packagePrefix));
        }

        return sb.toString();
    }
}
