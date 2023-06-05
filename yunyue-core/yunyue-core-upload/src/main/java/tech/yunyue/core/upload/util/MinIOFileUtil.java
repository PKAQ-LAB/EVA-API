package tech.yunyue.core.upload.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Upload;
import tech.yunyue.core.upload.condition.MinIOCondition;

import java.io.*;
import java.util.*;

/**
 * 文件上传工具类 - 使用MinIO
 */
@Slf4j
@Component
@Conditional(MinIOCondition.class)
@RequiredArgsConstructor
public class MinIOFileUtil implements FileProvider {
    private final static long SNOW = 16;
    private final static long FLAKE = 18;
    private final static  Snowflake snowflake = IdUtil.getSnowflake(SNOW, FLAKE);
    private final static String THUMBNAIL_NAME = "thumbnail_"; //缩略图前缀
    private static String temBucketName;
    private static String storageBucketName;
    private static String storeageImgBucketName;
    private static MinioClient minioClient;

    private final CacheManager cacheManager;
    private final EvaConfig evaConfig;


    /**
     * 初始化MinioClient和存储桶
     */
    @PostConstruct
    public void init() {
        Upload upload = evaConfig.getUpload();
        if (!StringUtils.hasText(upload.getMinioUrl())) {
            throw new BizException("请配置eva.upload.minio_url");
        }
        if (!StringUtils.hasText(upload.getMinioAccess())) {
            throw new BizException("请配置eva.upload.minio_access");
        }
        if (!StringUtils.hasText(upload.getMinioSecret())) {
            throw new BizException("请配置eva.upload.minio_secret");
        }

        //初始化MinioClient
        minioClient = MinioClient.builder()
                .endpoint(upload.getMinioUrl())
                .credentials(upload.getMinioAccess(), upload.getMinioSecret())
                .build();
        //创建存储桶 默认存储桶是私有的 只能通过外链访问 最长7天
        temBucketName = upload.getTempPath();
        storageBucketName = upload.getStoragePath();
        storeageImgBucketName = "img-" + storageBucketName;
        createBucket(temBucketName);
        createBucket(storageBucketName);
        createBucket(storeageImgBucketName);
        //设置图片桶的权限 可以通过服务器地址+桶名+文件名永久访问
        imgBucketPolicy();
    }
    /**
     * 文件上传 默认上传到配置的tmp目录
     *
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path){
        // 上传文件名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 存储后返回的信息
        String newFileName = "";

        if (StrUtil.isNotEmpty(fileName)) {
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            //非图片文件+:原名
            newFileName = snowflake.nextIdStr() + (isPicture(fileName) ? "." + suffixName : ":"+fileName);
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getMsg());
            throw new BizException(BizCodeEnum.FILENAME_ERROR);
        }
        // 判断上传文件是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().toLowerCase().contains(suffixName)) {
            //上传
            try{
                newFileName = uploadObject(file.getInputStream(),temBucketName, newFileName, true, file.getContentType());
            }catch (IOException e){
                log.error(e.getMessage());
                throw new BizException(BizCodeEnum.FILEIO_ERROR);
            }
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getMsg());
            throw new BizException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
        }
        // 放入缓存 todo 暂时
        this.cachePut(newFileName);
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
                                .bucket(getStorageBuketName(fileName))
                                .object(fileName)
                                .source(CopySource.builder()
                                                .bucket(temBucketName)
                                                .object(fileName)
                                                .build())
                                .build());

                //删除临时桶的文件
                removeMinio(temBucketName, fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
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
        for (String fileName : filenames) {
            // 判断文件格式是不是图片
            if(!isPicture(fileName)) continue;
            //保存到image storage桶中 保存成功则保存缩略图
            if(Objects.isNull(this.storage(fileName))){
                continue;
            }
            try(GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(storeageImgBucketName).object(fileName).build())) {
                ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();
                // 缩放后默认变成jpeg格式 用原来的后缀也能打开
                ImgUtil.scale(in, outThumbnail, scale);
                var suffix = fileName.substring(fileName.lastIndexOf(".")+1);
                // 缩略图的路径要与原图路径一致 所以不能根据当前时间生成文件夹
                var name = fileName.substring(fileName.lastIndexOf("/")+1);
                uploadObject(new ByteArrayInputStream(outThumbnail.toByteArray()), getStorageBuketName(fileName), fileName.replace(name, THUMBNAIL_NAME+name), false, "image/"+suffix);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale) {
    }

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale) {
    }

    /**
     * 从持久目录删除文件
     */
    @Override
    public void delFromStorage(String fileName) {
        String bucketName = getStorageBuketName(fileName);
        // 删除原文件
        removeMinio(bucketName, fileName);
        // 删除缩略图 不存在也不会报错
        String name = fileName.substring(fileName.lastIndexOf("/")+1);
        removeMinio(bucketName, fileName.replace(name, THUMBNAIL_NAME+name));

    }


    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);
        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(DateUtil.offsetHour(new Date(), -2), "HH");
        var cacheWrapper = cache.get(k);
        List<String> tmpFileList = null == cacheWrapper ? null : (List<String>) cacheWrapper.get();
        if (null == cacheWrapper || CollUtil.isEmpty(tmpFileList)) {
            return;
        }

        tmpFileList.stream().forEach(fileName -> {
            this.removeMinio(temBucketName, fileName);
        });
        // 删除完毕 从缓存中移除此key
        cache.evict(k);
    }

    /**
     * 默认从持久桶下载
     */
    @Override
    public void downLoad(String fileName, OutputStream out){
        try(GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder().bucket(getStorageBuketName(fileName)).object(fileName).build())) {
            IoUtil.copy(in, out, IoUtil.DEFAULT_BUFFER_SIZE);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 把临时文件名放到缓存中
     */
    private void cachePut(String fileName) {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(new Date(), "HH");
        Cache.ValueWrapper valueWrapper = cache.get(k);

        List<String> tmpFileList = new ArrayList<>();
        if (null == valueWrapper) {
            cache.put(k, tmpFileList);
        } else {
            tmpFileList = (List<String>) valueWrapper.get();
        }
        tmpFileList.add(fileName);
    }

    private static String getStorageBuketName(String fileName){
        return isPicture(fileName) ? storeageImgBucketName : storageBucketName;
    }

    /**
     * 查看存储桶是否存在
     */
    public static  Boolean bucketExists(String bucketName) {
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
    public static void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("创建桶[{}]失败：[{}]", bucketName, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 以流的方式上传文件到持久桶</br>
     * 文件名：文件夹路径+文件名
     * @param in 文件流
     * @param fileName 文件名
     * @return
     */
    public static String uploadObject(InputStream in, String fileName) {
        try {
            //上传
            uploadObject(in, getStorageBuketName(fileName), fileName, true, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 以流的方式上传文件</br>
     * 文件名：文件夹路径+文件名
     * @param in 文件流
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @return
     */
    public static String uploadObject(InputStream in, String bucketName, String fileName) {
        try {
            //上传
            uploadObject(in, bucketName, fileName, true, "application/octet-stream");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
    /**
     * 以流的方式上传文件</br>
     * 文件名：文件夹路径+文件名
     * @param in 文件流
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @param formatName 是否需要格式化文件名 即把文件名格式化成：文件夹名/文件名
     * @param contentType 文件的类型
     * @return
     */
    public static String uploadObject(InputStream in, String bucketName, String fileName, boolean formatName,  String contentType) {
        try {
            fileName = !formatName ? fileName : DateUtil.format(new Date(), "yyyy-MM") + "/" + DateUtil.format(new Date(), "dd") + "/" + fileName;
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
    public static void removeMinio(String bucketName,String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置图片桶的权限 可以通过服务器地址+桶名+文件名永久访问<br/>
     * 也可以直接在minio控制台设置
     */
    public static void imgBucketPolicy(){
        String bucketName = storeageImgBucketName;
        String policyJson ="{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetBucketLocation\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::"+bucketName+"\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetObject\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::"+bucketName+"/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        setBucketPolicy(bucketName, policyJson);
    }

    /**
     * 设置桶的权限
     */
    public static void setBucketPolicy(String bucketName, String policyJson){
        try {
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否为图片
     */
    public static boolean isPicture(String imgName) {
        return FileProvider.isPicture(imgName);
    }
}
