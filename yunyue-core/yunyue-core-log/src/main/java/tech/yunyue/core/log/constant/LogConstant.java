package tech.yunyue.core.log.constant;

/**
 * 日志常量
 */
public interface LogConstant {
    String EVENT_LOG = "BIZ-LOG";
    String TRANSACTIONAL_LOG = "TRANSACTIONAL-BIZ-LOG";

    String LOG_EXCHANGE = "xmc.log";

    String BIZ_LOG_DATE_FIELD = "operate_datetime";
    String BIZ_LOG_DB_NAME = "log_biz";
    String BIZ_LOG_HISTORY_DB_NAME = "history_log_biz";
    String BIZ_LOG_ROUTINGKEY = "bizlog";

    String ERROR_LOG_DATE_FIELD = "request_time";
    String ERROR_LOG_DB_NAME = "log_error";
    String ERROR_LOG_HISTORY_DB_NAME = "history_log_error";
    String ERROR_LOG_ROUTINGKEY = "errorlog";

    String LOGIN_LOG_DATE_FIELD = "operate_datetime";
    String LOGIN_DB_NAME = "log_login";
    String LOGIN_HISTORY_DB_NAME = "history_log_login";
    String LOGIN_LOG_ROUTINGKEY = "loginlog";
    String REPORT_DB_NAME = "log_report";
    String REPORT_HISTORY_DB_NAME = "history_log_report";
    String REPORT_COUNT_DB_NAME = "log_report_print_count";
    String REPORT_COUNT_HISTORY_DB_NAME = "history_log_report_print_count";
    String REPORT_LOG_ROUTINGKEY = "reportlog";
    // mongo历史记录表的唯一标识 不一定每张表都用id做标识字段
    String MONGO_HISTORY_TABLE_MARK = "m_mark_id";
    // mongo历史记录表的创建时间
    String MONGO_CREATE_TIME = "mCreateTime";
    // mongo历史记录表的创建人
    String MONGO_CREATE_NAME = "mCreateName";
    // mongo TTL索引使用
    String MONGO_EXPIRE_TIME = "m_expire_time";


}
