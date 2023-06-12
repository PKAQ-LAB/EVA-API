package tech.yunyue.core.upload.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.NioUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Upload;
import tech.yunyue.core.upload.condition.MinIOCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

/**
 * 文件上传工具类 - 使用MinIO
 * <br/> 存储桶名称的长度必须介于 3（分钟）到 63（最大）个字符之间。
 * <br/> 存储桶名称只能由小写字母、数字、点 （.） 和连字符组成 (-).
 * <br/> 存储桶名称必须以字母或数字开头和结尾。
 * <br/> 存储桶名称不得包含两个相邻的句点。
 * <br/> 存储桶名称不得格式化为 IP 地址（例如， 192.168.5.4).
 */
@Slf4j
@Component
@Conditional(MinIOCondition.class)
@ConditionalOnMissingBean(Upload.MinIO.class)
@RequiredArgsConstructor
public class MinIOFileUtil implements FileProvider {
    private final EvaConfig evaConfig;
    private static MinioClient minioClient;
    //缩略图前缀
    private static final String THUMBNAIL_NAME = "thumbnail_";
    private static final String TEMP = "temp";
    private static final String STORAGE = "storage";
    private static final String IMAGE = "images";
    private static final Snowflake snowflake = IdUtil.getSnowflake(16, 18);

    /**
     * 初始化MinioClient和存储桶
     */
    @PostConstruct
    public void init() {
        Upload.MinIO minIo = evaConfig.getUpload().getMinIo();
        if (!StringUtils.hasText(minIo.getUrl())) {
            throw new BizException("请配置eva.upload.minio.url");
        }
        if (!StringUtils.hasText(minIo.getAccess())) {
            throw new BizException("请配置eva.upload.minio.access");
        }
        if (!StringUtils.hasText(minIo.getSecret())) {
            throw new BizException("请配置eva.upload.minio.secret");
        }
        //初始化MinioClient
        minioClient = MinioClient.builder()
                .endpoint(minIo.getUrl())
                .credentials(minIo.getAccess(), minIo.getSecret())
                .build();

        //创建存储桶 默认存储桶是私有的 只能通过外链访问 最长7天
        //新增存储桶images文件夹的策略 改成readonly即可实现通过链接访问图片 但是访问不了该桶内别的文件
        createBucket(TEMP);
        createBucket(STORAGE);
    }
    @Bean
    public MinioClient minioClient() {
        return minioClient;
    }

    /**
     * 文件上传 默认上传到配置的tmp目录
     * 按文件类型/YYYYMM结构存储文件
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path) throws Exception {
        // 上传文件名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = FileUtil.extName(fileName);
        // 存储后返回的信息
        String newFileName = "";

        boolean isPic = isPicture(file);
        String fileType =isPic ? IMAGE : FileTypeUtil.getType(file.getInputStream(), fileName);
        String times = DateUtil.format(new Date(),"yyyyMM");
        String day = DateUtil.format(new Date(), "dd");
        String dirName = fileType + "/" + times + "/" + day+ "/";

        if (CharSequenceUtil.isNotEmpty(fileName)) {
            //非图片文件名 xxxxx:原名
            newFileName = snowflake.nextIdStr() + (isPic ? "." + suffixName : ":" + fileName);
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getMsg());
            throw new BizException(BizCodeEnum.FILENAME_ERROR);
        }
        // 判断上传文件是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().toLowerCase().contains(suffixName)) {
            //上传
            try {
                newFileName = uploadObject(file.getInputStream(), TEMP, dirName + newFileName, false, file.getContentType());
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new BizException(BizCodeEnum.FILEIO_ERROR);
            }
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getMsg());
            throw new BizException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
        }
        return newFileName;
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
                                .bucket(STORAGE)
                                .object(fileName)
                                .source(CopySource.builder()
                                        .bucket(TEMP)
                                        .object(fileName)
                                        .build())
                                .build());

                //删除临时桶的文件
                removeMinio(TEMP, fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return List.of(filenames);
    }

    /**
     * 将文件从缓存目录移动到storage目录 如果是图片则生成缩略图
     *
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames) {
        //保存到持久桶中 如果是图片则生成缩略图并保存
        Arrays.stream(filenames)
                .filter(fileName -> Objects.nonNull(this.storage(fileName)))
                .filter(fileName -> fileName.startsWith(IMAGE + "/"))
                .forEach(fileName -> {
                    try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(STORAGE).object(fileName).build())) {
                        ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();

                        // 缩放后默认变成jpeg格式 用原来的后缀也能打开
                        ImgUtil.scale(in, outThumbnail, scale);

                        // 缩略图的路径要与原图路径一致 所以不能根据当前时间生成文件夹
                        var name = fileName.substring(fileName.lastIndexOf("/") + 1);
                        uploadObject(new ByteArrayInputStream(outThumbnail.toByteArray()), STORAGE,
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
     */
    @Override
    public void delFromStorage(String fileName) {
        // 删除原文件
        removeMinio(STORAGE, fileName);
        // 删除缩略图 不存在也不会报错
        String name = fileName.substring(fileName.lastIndexOf("/") + 1);
        removeMinio(STORAGE, fileName.replace(name, THUMBNAIL_NAME + name));

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
        try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(STORAGE).object(fileName).build())) {
            IoUtil.copy(in, out, NioUtil.DEFAULT_BUFFER_SIZE);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 查看存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName))
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("创建桶[{}]失败：[{}]", bucketName, e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
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
            uploadObject(in, bucketName, fileName, true,  MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
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
            fileName = !formatName ? fileName : DateUtil.format(new Date(), "yyyyMM") + "/" + DateUtil.format(new Date(), "dd") + "/" + fileName;
            //上传
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    in, in.available(), -1)
                            .contentType(contentType)
                            .build());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 删除文件
     */
    public void removeMinio(String bucketName, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置桶的权限
     */
    public void setBucketPolicy(String bucketName, String policyJson) {
        try {
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否为图片
     */
    private boolean isPicture(MultipartFile file) throws IOException {
        String suffixStr = ".bmp .dib .gif .jfif .jpe .jpeg .jpg .png .tif .tiff .ico";
        String fileType = FileTypeUtil.getType(file.getInputStream());
        return Objects.nonNull(fileType) && suffixStr.contains(fileType);
    }
}
