package io.nerv.core.upload.job;

/**
 * 缓存目录文件清理任务
 * 定时清理因用户上传后未提交表单产生的临时文件
 *
 * 举个例子，比如 我有个表单，里面有合同上传，文件是异步上传到文件服务器然后返回文件id的,
 * 然后这个人一直上传 就是不提交表单，那么它上传的这个合同文件是不是没有任何价值
 */
public class TempFileCleanTask {
}
