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


}
