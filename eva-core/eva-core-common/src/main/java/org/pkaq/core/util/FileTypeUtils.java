package org.pkaq.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单文件类型识别工具（基于魔数 / 文件头）
 * 仅依赖 JDK，无第三方库
 */
public class FileTypeUtils {

    // 常见文件魔数映射
    private static final Map<String, String> MAGIC_NUMBER_MAP = new HashMap<>();

    static {
        MAGIC_NUMBER_MAP.put("FFD8FF", "jpg");
        MAGIC_NUMBER_MAP.put("89504E47", "png");
        MAGIC_NUMBER_MAP.put("47494638", "gif");
        MAGIC_NUMBER_MAP.put("25504446", "pdf");
        MAGIC_NUMBER_MAP.put("504B0304", "zip");
        MAGIC_NUMBER_MAP.put("504B34", "zip");
        MAGIC_NUMBER_MAP.put("504B0506", "zip"); 
        MAGIC_NUMBER_MAP.put("504B0708", "zip");
        MAGIC_NUMBER_MAP.put("52617221", "rar");
        MAGIC_NUMBER_MAP.put("1F8B08", "gz");
        MAGIC_NUMBER_MAP.put("424D", "bmp");
        MAGIC_NUMBER_MAP.put("49492A00", "tif");
        MAGIC_NUMBER_MAP.put("000001BA", "mpg");
        MAGIC_NUMBER_MAP.put("000001B3", "mpg");
        MAGIC_NUMBER_MAP.put("66747970", "mp4");
        MAGIC_NUMBER_MAP.put("3026B2758E66CF11", "wmv");
        MAGIC_NUMBER_MAP.put("57415645", "wav");
        MAGIC_NUMBER_MAP.put("41564920", "avi");
        MAGIC_NUMBER_MAP.put("4D546864", "mid");
        MAGIC_NUMBER_MAP.put("00000020", "mp4");
        // 可继续扩展更多常用类型
    }

    /**
     * 获取文件类型（扩展名）  
     * @param inputStream 文件输入流
     * @return 扩展名，例如 "jpg", "png", "pdf"，无法识别返回 null
     * @throws IOException
     */
    public static String getType(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        inputStream.mark(20);
        byte[] firstBytes = new byte[16];
        int read = inputStream.read(firstBytes);
        inputStream.reset();

        if (read <= 0) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < read; i++) {
            sb.append(String.format("%02X", firstBytes[i]));
        }

        String fileHeader = sb.toString();

        for (Map.Entry<String, String> entry : MAGIC_NUMBER_MAP.entrySet()) {
            if (fileHeader.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}
