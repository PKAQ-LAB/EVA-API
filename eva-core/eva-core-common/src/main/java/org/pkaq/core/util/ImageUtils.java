package org.pkaq.core.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 纯 JDK 图片缩放工具类（替代
 */
public class ImageUtils {

    /**
     * 按比例缩放图片（文件版）
     *
     * @param srcFile  源图片文件
     * @param destFile 目标输出文件
     * @param scale    缩放比例（如 0.5 表示缩小一半）
     * @throws IOException 读写异常
     */
    public static void scale(File srcFile, File destFile, double scale) throws IOException {
        if (srcFile == null || destFile == null) {
            throw new IllegalArgumentException("srcFile 和 destFile 不能为空");
        }

        try (InputStream in = new FileInputStream(srcFile);
             OutputStream out = new FileOutputStream(destFile)) {
            scale(in, out, (float) scale);
        }
    }

    /**
     * 按比例缩放图片（流版）
     *
     * @param srcStream  源图片输入流
     * @param destStream 输出图片流
     * @param scale      缩放比例（如 0.5 表示缩小一半）
     * @throws IOException IO错误或图片格式错误
     */
    public static void scale(InputStream srcStream, OutputStream destStream, float scale) throws IOException {
        if (srcStream == null || destStream == null) {
            throw new IllegalArgumentException("输入流或输出流不能为空");
        }

        BufferedImage srcImage = ImageIO.read(srcStream);
        if (srcImage == null) {
            throw new IOException("无法读取图片数据，可能不是有效的图像格式");
        }

        int width = Math.max(1, Math.round(srcImage.getWidth() * scale));
        int height = Math.max(1, Math.round(srcImage.getHeight() * scale));

        // 创建目标图像（保持透明背景）
        int imageType = srcImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : srcImage.getType();
        BufferedImage scaledImage = new BufferedImage(width, height, imageType);

        Graphics2D g2d = scaledImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(srcImage, 0, 0, width, height, null);
        } finally {
            g2d.dispose();
        }

        // 默认输出 PNG（兼容透明背景）
        ImageIO.write(scaledImage, "png", destStream);
    }

    /**
     * 获取文件扩展名（辅助方法）
     */
    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < filename.length() - 1)
                ? filename.substring(dotIndex + 1).toLowerCase()
                : null;
    }
}
