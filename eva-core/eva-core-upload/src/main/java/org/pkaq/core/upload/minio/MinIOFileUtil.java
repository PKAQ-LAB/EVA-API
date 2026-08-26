package org.pkaq.core.upload.minio;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.upload.condition.MinIOCondition;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.FileTypeUtils;
import org.pkaq.core.util.IOUtils;
import org.pkaq.core.util.ImageUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * 文件上传工具类 - 使用MinIO
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(MinIOCondition.class)
@RequiredArgsConstructor
public class MinIOFileUtil implements FileProvider {
    //缩略图前缀
    private static final String THUMBNAIL_NAME = "thumbnail_";
    private static final String TEMP = "_temp";
    private static final String STORAGE = "_storage";
    private final MinioClient minioClient;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * 按文件类型/YYYYMM结构存储文件
     *
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path) throws Exception {
        // 判断桶是否存在 fileType/yyyyMM
        String fileType = FileTypeUtils.getType(file.getInputStream());
        String times = DateUtils.format(new Date(), "yyyyMM");
        String dirName = TEMP + fileType + "/" + times;

        // 目标桶不存在 新建
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(dirName).build())) {
            createBucket(dirName);
        }

        return "";

        // 上传文件名
//        String fileName = file.getOriginalFilename();
//        // 后缀名
//        String suffixName = "";
//        // 存储后返回的信息
//        String newFileName = "";
//
//        if (StrUtils.isNotEmpty(fileName)) {
//            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//            //非图片文件+:原名
//            newFileName = snowflake.nextIdStr() + ":" + fileName;
//        } else {
//            log.error(BizCodeEnum.FILEIO_ERROR.getMsg());
//            throw new BizException(BizCodeEnum.FILENAME_ERROR);
//        }
//        // 判断上传文件是否符合格式
//        if (evaConfig.getUpload().getAllowSuffixName().toLowerCase().contains(suffixName)) {
//            //上传
//            try {
//                newFileName = uploadObject(file.getInputStream(), TEMP, newFileName, true, file.getContentType());
//            } catch (IOException e) {
//                log.error(e.getMessage());
//                throw new BizException(BizCodeEnum.FILEIO_ERROR);
//            }
//        } else {
//            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getMsg());
//            throw new BizException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
//        }
//        return newFileName;
    }


    /**
     * 将文件从缓存目录移动到storage目录
     *
     * @param filenames
     * @return
     */
    @Override
    public List<String> storage(String... filenames) {
        for (String fileName : filenames) {
            try {
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(fileName)
                                .object(fileName)
                                .source(SourceObject.builder()
                                        .bucket(TEMP)
                                        .object(fileName)
                                        .build())
                                .build());

                //删除临时桶的文件
                removeMinio(TEMP, fileName);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Collections.emptyList();
            }
        }
        return List.of(filenames);
    }

    /**
     * 将文件从缓存目录移动到storage目录并生成缩略图
     *
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames) {
        // 判断文件格式是不是图片
        //保存到image storage桶中 保存成功则保存缩略图
        Arrays.stream(filenames)
                .filter(fileName -> !Objects.isNull(this.storage(fileName)))
                .forEach(fileName -> {
                    try (GetObjectResponse in = this.minioClient.getObject(GetObjectArgs.builder().bucket(STORAGE).object(fileName).build())) {
                        ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();

                        // 缩放后默认变成jpeg格式 用原来的后缀也能打开
                        ImageUtils.scale(in, outThumbnail, scale);
                        var suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

                        // 缩略图的路径要与原图路径一致 所以不能根据当前时间生成文件夹
                        var name = fileName.substring(fileName.lastIndexOf("/") + 1);
                        uploadObject(new ByteArrayInputStream(outThumbnail.toByteArray()), fileName,
                                fileName.replace(name, THUMBNAIL_NAME + name),
                                false,
                                "image/" + suffix);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
    }

    /**
     * 生成缩略图
     *
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale) {
        throw new UnsupportedOperationException("MinIO provider does not support local File thumbnails");
    }

    /**
     * 生成缩略图
     *
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale) {
        throw new UnsupportedOperationException("MinIO provider does not support local File thumbnails");
    }

    /**
     * 从持久目录删除文件
     */
    @Override
    public void delFromStorage(String fileName) {
        String bucketName = fileName;
        // 删除原文件
        removeMinio(bucketName, fileName);
        // 删除缩略图 不存在也不会报错
        String name = fileName.substring(fileName.lastIndexOf("/") + 1);
        removeMinio(bucketName, fileName.replace(name, THUMBNAIL_NAME + name));

    }


    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        // minio桶可以设置自动对象过期 自动删除过期对象
    }

    /**
     * 默认从持久桶下载
     */
    @Override
    public void downLoad(String fileName, OutputStream out) {
        try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(fileName).object(fileName).build())) {
            IOUtils.copy(in, out, IOUtils.DEFAULT_BUFFER_SIZE);
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 查看存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName))
                this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("创建桶[{}]失败：[{}]", bucketName, e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 以流的方式上传文件到持久桶</br>
     * 文件名：文件夹路径+文件名
     *
     * @param in       文件流
     * @param fileName 文件名
     * @return
     */
    public String uploadObject(InputStream in, String fileName) {
        try {
            //上传
            uploadObject(in, fileName, fileName, true, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fileName;
    }

    /**
     * 以流的方式上传文件</br>
     * 文件名：文件夹路径+文件名
     *
     * @param in         文件流
     * @param bucketName 存储桶名
     * @param fileName   文件名
     * @return
     */
    public String uploadObject(InputStream in, String bucketName, String fileName) {
        try {
            //上传
            uploadObject(in, bucketName, fileName, true, "application/octet-stream");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fileName;
    }

    /**
     * 以流的方式上传文件</br>
     * 文件名：文件夹路径+文件名
     *
     * @param in          文件流
     * @param bucketName  存储桶名
     * @param fileName    文件名
     * @param formatName  是否需要格式化文件名 即把文件名格式化成：文件夹名/文件名
     * @param contentType 文件的类型
     * @return
     */
    public String uploadObject(InputStream in, String bucketName, String fileName, boolean formatName, String contentType) {
        try {
            fileName = !formatName ? fileName : DateUtils.format(new Date(), "yyyy-MM") + "/" + DateUtils.format(new Date(), "dd") + "/" + fileName;
            //上传
            this.minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    in, (long) in.available(), -1L)
                            .contentType(contentType)
                            .build());
            in.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fileName;
    }

    /**
     * 删除文件
     */
    public void removeMinio(String bucketName, String fileName) {
        try {
            this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 设置桶的权限
     */
    public void setBucketPolicy(String bucketName, String policyJson) {
        try {
            this.minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断文件是否为图片
     */
    private boolean isPicture(MultipartFile file) throws IOException {
        String suffixStr = ".bmp .dib .gif .jfif .jpe .jpeg .jpg .png .tif .tiff .ico";
        String fileType = FileTypeUtils.getType(file.getInputStream());
        return suffixStr.contains(fileType);
    }
}
