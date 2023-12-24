package tech.yunyue.core.upload.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.NioUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.upload.condition.MinIOCondition;
import tech.yunyue.core.upload.enumm.MinIOBucketEnum;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件上传工具类 - 使用MinIO
 */
@Slf4j
@Component
@Conditional(MinIOCondition.class)
@RequiredArgsConstructor
public class MinIOFileUtil implements FileProvider {
    private final EvaConfig evaConfig;
    private final MinioClient minioClient;
    public FileProvider self;
    // 缩略图前缀
    private static final String THUMBNAIL_NAME = "thumbnail_";
    private static final String IMAGE = "images";
    private static final Snowflake snowflake = IdUtil.getSnowflake(16, 18);
    private static final Map<String, DataSize> SUFFIX_MAX_SIZE_MAP = new HashMap<>();

    /**
     * 初始化文件桶
     */
    @PostConstruct
    public void init() {
        // 创建存储桶 默认存储桶是私有的 只能通过外链访问 最长7天
        // 新增存储桶images文件夹的策略 改成readonly即可实现通过链接访问图片 但是访问不了该桶内别的文件
        Arrays.stream(MinIOBucketEnum.values()).forEach(bucket -> createBucket(bucket.getBucketName()));

        // 初始化后缀限制的文件大小  系统配置的好几个后缀对应一个限制长度，拆分成每个后缀对应一个限制长度
        var sysMap = evaConfig.getUpload().getSuffixMaxSize();
        Optional.ofNullable(sysMap).ifPresent(map -> {
            map.forEach((key, value) -> {
                for (String suffix : key.split(",")) {
                    SUFFIX_MAX_SIZE_MAP.put(suffix.trim(), value);
                }
            });
        });
    }

    /**
     * 查看存储桶是否存在
     */
    private boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("BucketExists ERROR : ", e);
            return false;
        }
    }

    /**
     * 创建桶
     */
    private void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("创建桶[{}]失败：[{}]", bucketName, e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param bucketName 存储桶
     * @param objectName 对象
     * @return true：存在
     */
    public boolean objectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            minioClient
                    .statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 判断文件类型
     *
     * @param file 文件
     * @return
     */
    private String getFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            String fileType = FileTypeUtil.getType(file.getInputStream(), fileName);
            return SUFFIXSTR.contains(fileType) ? IMAGE : fileType;
        } catch (Exception e) {
            return FileUtil.extName(fileName);
        }
    }

    /**
     * 上传文件到temp
     * 按文件类型/YYYYMM结构存储文件
     *
     * @param file
     * @return 生成的文件目录+保存的文件名
     */
    @Override
    public String upload(MultipartFile file) {
        // 上传文件名
        String fileName = file.getOriginalFilename();
        if (CharSequenceUtil.isBlank(fileName)) {
            log.error(BizCodeEnum.FILEIO_ERROR.getMsg());
            BizCodeEnum.FILENAME_ERROR.newException();
        }
        // 后缀名
        String suffixName = FileUtil.extName(fileName);

        String fileType = getFileType(file);

        String path = "%s/%s/".formatted(fileType, DateUtil.format(new Date(), "yyyyMM/dd"));
        // 非图片文件名 xxxxx:原名
        String newFileName = snowflake.nextIdStr() + (IMAGE.equals(fileType) ? "." + suffixName : ":" + fileName);

        return uploadFile(file, MinIOBucketEnum.TEMP, path + newFileName);
    }

    /**
     * 上传文件到临时捅的path目录下
     *
     * @param file 文件
     * @param path 目录
     * @return path+文件原名
     */
    @Override
    public String upload(MultipartFile file, String path) {
        return upload(file, MinIOBucketEnum.TEMP, path);
    }

    /**
     * 上传文件到target的path目录下
     *
     * @param file   文件
     * @param target 桶名
     * @param path   目录
     * @return 目录+文件原名
     * @throws Exception
     */
    @Override
    public String upload(MultipartFile file, MinIOBucketEnum target, String path) {
        // 上传文件名
        String fileName = file.getOriginalFilename();
        fileName = "/%s/%s".formatted(path.replaceAll("^/|/$", ""), fileName);
        return uploadFile(file, target, fileName);
    }

    /**
     * 上传文件到目标桶，并且指定文件名
     *
     * @param file     文件流
     * @param target   目标桶
     * @param fileName 文件名
     * @return 文件名
     */
    private String uploadFile(MultipartFile file, MinIOBucketEnum target, String fileName) {
        var uploadConfig = evaConfig.getUpload();
        // 后缀名
        String suffixName = FileUtil.extName(fileName);
        // 判断上传文件是否符合格式
        String typeLimit = uploadConfig.getAllowSuffixName().toLowerCase();
        // 判断文件大小是否符合系统配置大小
        AtomicBoolean isSizeValid = new AtomicBoolean(false);
        Optional.ofNullable(SUFFIX_MAX_SIZE_MAP.get(StrPool.DOT + suffixName)).ifPresent(sysMaxSize -> {
            var fileSize = DataSize.ofBytes(file.getSize());
            if (fileSize.compareTo(sysMaxSize) > 0) {
                BizCodeEnum.FILE_SIZE_EXCEEDS_LIMIT.newException();
            }
            isSizeValid.set(true);
        });
        // 处理文件上传逻辑，例如保存文件到服务器
        if (isSizeValid.get() &&
                CharSequenceUtil.isNotBlank(typeLimit) &&
                !"*".equals(typeLimit) &&
                typeLimit.contains(suffixName)) {
            // 上传
            try {
                fileName = uploadObject(file.getInputStream(), target, fileName, false, file.getContentType());
            } catch (IOException e) {
                log.error(e.getMessage());
                BizCodeEnum.FILEIO_ERROR.newException();
            }
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getMsg());
            BizCodeEnum.FILETYPE_NOT_SUPPORTED.newException();
        }
        return fileName;
    }

    /**
     * 将文件从临时目录转移到持久目录 并删除源文件
     *
     * @param filenames 需要转移的文件名
     * @return
     */
    @Override
    public List<String> storage(String... filenames) {
        getSelf().storage(MinIOBucketEnum.TEMP, MinIOBucketEnum.STORAGE, filenames);
        return List.of(filenames);
    }

    /**
     * 将文件从临时目录转移到目标桶 并删除源文件
     *
     * @param target    目标桶
     * @param filenames 需要转移的文件名
     */
    @Async("file_task")
    @Override
    public void storage(MinIOBucketEnum target, String... filenames) {
        storage(MinIOBucketEnum.TEMP, target, filenames);
    }

    /**
     * 将文件从源桶转移到目标桶 并删除源文件
     *
     * @param source    源桶
     * @param target    目标桶
     * @param filenames 文件名
     */
    @Async("file_task")
    @Override
    public void storage(MinIOBucketEnum source, MinIOBucketEnum target, String... filenames) {
        for (String fileName : filenames) {
            try {
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(target.getBucketName())
                                .object(fileName)
                                .source(CopySource.builder()
                                        .bucket(source.getBucketName())
                                        .object(fileName)
                                        .build())
                                .build());

                // 删除源桶的文件
                delete(source, fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将文件从临时目录移动到持久目录 是图片则根据配置的长宽生成缩略图 300*300
     *
     * @param filenames 文件名
     */
    @Override
    public void storageWithThumbnail(String... filenames) {
        var upload = evaConfig.getUpload();
        storageWithThumbnail(upload.getScaleWidth(), upload.getScaleHeight(), filenames);
    }

    /**
     * 将文件从缓存目录移动到storage目录 是图片则按比例生成缩略图
     *
     * @param scale
     * @param filenames
     */
    @Async("file_task")
    @Override
    public void storageWithThumbnail(float scale, String... filenames) {
        storageWithThumbnail(scale, 0, 0, filenames);
    }


    /**
     * 将文件从缓存目录移动到storage目录 是图片则按长宽生成缩略图
     *
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(int width, int height, String... filenames) {
        storageWithThumbnail(Float.NaN, width, height, filenames);
    }

    /**
     * 将文件从缓存目录移动到storage目录 如果是图片则按比例/长宽生成缩略图
     *
     * @param scale     缩放比例 Float.NaN时按长宽缩放
     * @param width     宽
     * @param height    长
     * @param filenames 文件名
     */
    private void storageWithThumbnail(float scale, int width, int height, String... filenames) {
        // 保存到持久桶中 如果是图片则生成缩略图并保存
        Arrays.stream(filenames)
                .filter(fileName -> {
                    this.storage(MinIOBucketEnum.STORAGE, fileName);
                    return fileName.startsWith(IMAGE + "/");
                })
                .forEach(fileName -> {
                    try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(MinIOBucketEnum.STORAGE.getBucketName()).object(fileName).build())) {
                        ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();

                        // 缩放后默认变成jpeg格式 用原来的后缀也能打开
                        if (Float.isNaN(scale)) {
                            ImgUtil.write(ImgUtil.scale(ImgUtil.read(in), width, height), FileUtil.extName(fileName), outThumbnail);
                        } else {
                            ImgUtil.scale(in, outThumbnail, scale);
                        }
                        // 缩略图的路径要与原图路径一致 所以不能根据当前时间生成文件夹
                        var name = fileName.substring(fileName.lastIndexOf("/") + 1);
                        uploadObject(new ByteArrayInputStream(outThumbnail.toByteArray()), MinIOBucketEnum.STORAGE,
                                fileName.replace(name, THUMBNAIL_NAME + name),
                                false,
                                "image/" + FileUtil.extName(fileName));

                    } catch (Exception e) {
                        e.printStackTrace();
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
        // TODO document why this method is empty
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
        // TODO document why this method is empty
    }

    /**
     * 从持久目录删除文件
     *
     * @param fileName 文件名
     */
    @Async("file_task")
    @Override
    public void delFromStorage(String fileName) {
        // 删除原文件
        delete(MinIOBucketEnum.STORAGE, fileName);
        // 删除缩略图 不存在也不会报错
        String name = fileName.substring(fileName.lastIndexOf("/") + 1);
        delete(MinIOBucketEnum.STORAGE, fileName.replace(name, THUMBNAIL_NAME + name));

    }

    /**
     * 从指定桶删除文件
     *
     * @param fileName 文件名
     */
    @Async("file_task")
    @Override
    public void delete(MinIOBucketEnum target, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(target.getBucketName()).object(fileName).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从持久目录删除文件（逻辑删除，转移到归档桶）
     *
     * @param fileName 文件名
     */
    @Async("file_task")
    @Override
    public void deleteLogic(String fileName) {
        deleteLogic(MinIOBucketEnum.STORAGE, fileName);
    }

    /**
     * 从source删除文件（逻辑删除，转移到归档桶）
     *
     * @param fileName 文件名
     */
    @Async("file_task")
    @Override
    public void deleteLogic(MinIOBucketEnum source, String fileName) {
        // 归档并删除源文件
        storage(source, MinIOBucketEnum.ARCHIVE, fileName);
        // 删除缩略图 不存在也不会报错
        String name = fileName.substring(fileName.lastIndexOf("/") + 1);
        delete(source, fileName.replace(name, THUMBNAIL_NAME + name));
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        // minio桶可以设置自动对象过期 自动删除过期对象
    }

    /**
     * 从持久桶下载文件
     *
     * @param fileName 文件名
     * @param out      输出流
     */
    @Override
    public void downLoad(String fileName, OutputStream out) {
        downLoad(fileName, out, MinIOBucketEnum.STORAGE);
    }

    /**
     * 从目标桶下载文件
     *
     * @param fileName 文件名
     * @param target   桶名
     * @param out      输出流
     */
    @Override
    public void downLoad(String fileName, OutputStream out, MinIOBucketEnum target) {
        try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(target.getBucketName()).object(fileName).build())) {
            IoUtil.copy(in, out, NioUtil.DEFAULT_BUFFER_SIZE);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 以流的方式上传文件</br>
     * 文件名：文件夹路径+文件名
     *
     * @param in          文件流
     * @param bucketEnum  存储桶名
     * @param fileName    文件名
     * @param formatName  是否需要格式化文件名 即把文件名格式化成：文件夹名/文件名
     * @param contentType 文件的类型
     * @return
     */
    public String uploadObject(InputStream in, MinIOBucketEnum bucketEnum, String fileName, boolean formatName, String contentType) {
        try {
            fileName = !formatName ? fileName : "%s/%s".formatted(DateUtil.format(new Date(), "yyyyMM/dd"), fileName);
            // 上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketEnum.getBucketName()).object(fileName).stream(in, in.available(), -1)
                            .contentType(contentType)
                            .build());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 查看持久桶中该文件的缩略图
     *
     * @param fileName 文件名
     * @return 文件的缩略图预览url
     */
    @Override
    public String previewThumbnail(String fileName) {
        if (CharSequenceUtil.isBlank(fileName)) {
            return null;
        }
        var name = fileName.substring(fileName.lastIndexOf("/") + 1);
        fileName = fileName.replace(name, THUMBNAIL_NAME + name);
        return preview(fileName, MinIOBucketEnum.STORAGE);
    }

    /**
     * 预览持久桶中的文件
     *
     * @param fileName 文件名
     * @return 文件的预览url
     */
    @Override
    public String preview(String fileName) {
        return preview(fileName, MinIOBucketEnum.STORAGE);
    }

    /**
     * 预览目标桶的文件
     *
     * @param fileName 文件名
     * @param target   目标桶
     * @return 文件的预览url
     */
    @Override
    public String preview(String fileName, MinIOBucketEnum target) {
        if (CharSequenceUtil.isBlank(fileName)) {
            return null;
        }
        try {
            // 5分钟过期
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(target.getBucketName())
                            .object(fileName)
                            .expiry(5, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据图片的预览链接返回源文件名
     *
     * @param previewUrl 持久桶的预览连接或者源文件名
     * @return 原文件名
     */
    @Override
    public String parsePreviewUrlToFileName(String previewUrl) {
        return parsePreviewUrlToFileName(MinIOBucketEnum.STORAGE, previewUrl);
    }

    /**
     * @param fileName 文件名
     * @return 获取持久桶的文件流
     */
    @Override
    public InputStream getFileInputStream(String fileName) {
        return getFileInputStream(fileName, MinIOBucketEnum.STORAGE);
    }

    /**
     * @param fileName 文件名
     * @param target   目标桶
     * @return 获取目标桶对应文件的文件流
     */
    @Override
    public InputStream getFileInputStream(String fileName, MinIOBucketEnum target) {
        if (CharSequenceUtil.isBlank(fileName)) {
            return FileProvider.super.getFileInputStream(fileName, target);
        }
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(target.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("获取[{}]桶[{}]文件输入流失败：[{}]", target.getBucketName(), fileName, e.getMessage());
        }
        return FileProvider.super.getFileInputStream(fileName, target);
    }

    private String parsePreviewUrlToFileName(MinIOBucketEnum bucketEnum, String previewUrl) {
        String thumbnailPattern = "^http[^?]+/%s/([^?]+)%s([^?]+)\\\\?".formatted(bucketEnum.getBucketName(), THUMBNAIL_NAME);
        // 缩略图的预览url
        Matcher matcher = Pattern.compile(thumbnailPattern).matcher(previewUrl);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2);
        }
        return previewUrl;
    }

    FileProvider getSelf() {
        if (self == null) {
            self = SpringUtil.getBean(FileProvider.class);
        }
        return self;
    }
}
