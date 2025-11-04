package org.pkaq.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class FileUtils {
    public static void move(File src, File target, boolean isOverride) throws IOException {
        Files.move(
                src.toPath(),
                target.toPath(),
                isOverride? StandardCopyOption.REPLACE_EXISTING: StandardCopyOption.COPY_ATTRIBUTES
        );
    }

    public static boolean isDirEmpty(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return true;
        }
        String[] files = folder.list();
        return files == null || files.length == 0;
    }

    public static boolean del(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        Path path = file.toPath();
        try {
            if (file.isDirectory()) {
                // 递归删除目录
                Files.walk(path)
                        // 先删子文件，再删父目录
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                throw new RuntimeException("删除失败: " + p, e);
                            }
                        });
            } else {
                // 删除单个文件
                Files.deleteIfExists(path);
            }
            return true;
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
