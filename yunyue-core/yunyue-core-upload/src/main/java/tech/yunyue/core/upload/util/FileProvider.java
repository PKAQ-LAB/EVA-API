package tech.yunyue.core.upload.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传下载接口
 */
public interface FileProvider {
    /**
     * 上传
     *
     * @param file
     * @return
     */
    String upload(MultipartFile file, String path) throws Exception;

    /**
     * 存储
     *
     * @param filenames
     * @return
     */
    List<String> storage(String... filenames);

    /**
     * 将文件从缓存目录移动到storage目录 是图片则根据配置的长宽生成缩略图 300*300
     * @param filenames
     */
    default void storageWithThumbnail(String... filenames){};

    /**
     * 将文件从缓存目录移动到storage目录 是图片则按比例生成缩略图
     *
     * @param scale
     * @param filenames
     */
    void storageWithThumbnail(float scale, String... filenames);

    /**
     * 将文件从缓存目录移动到storage目录 是图片则按长宽生成缩略图
     * @param filenames
     */
    default void storageWithThumbnail(int width, int height, String... filenames){};
    /**
     * 生成缩略图
     *
     * @param file
     * @param scale
     */
    void thumbnail(File file, float scale);

    /**
     * 生成缩略图,可指定缩放
     *
     * @param file
     * @param dest
     * @param scale
     */
    void thumbnail(File file, File dest, float scale);

    /**
     * 删除图片
     *
     * @param fileName
     */
    void delFromStorage(String fileName);

    /**
     * 缓存清除
     */
    void tempClean();

    /**
     * 下载文件
     */
    default void downLoad(String fileName, OutputStream out){};

    /**
     * @param fileName 文件名
     * @return 根据文件名称 生成临时桶的预览url
     */
    default String previewTemp(String fileName){
        return "";
    };

    /**
     * @param isThumbnail 是否生成缩略图的预览url
     * @param fileName 文件名
     * @return 根据文件名称 生成持久桶缩略图/原图的预览url
     */
    default String preview(Boolean isThumbnail, String fileName){
        return "";
    };
    /**
     * @param fileNames 文件名集合
     * @return 根据文件名称 批量生成预览url
     */
    default List<String> preview(List<String> fileNames){
        return fileNames.stream().map(name -> preview(false,name)).collect(Collectors.toList());
    };

}
