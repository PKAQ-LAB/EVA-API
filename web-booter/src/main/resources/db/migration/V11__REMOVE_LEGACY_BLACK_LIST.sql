-- IP、Consumer 与 URI 黑白名单统一由 APISIX 管理，应用不再保留重复配置表。
DROP TABLE IF EXISTS SYS_BLACK_LIST;
