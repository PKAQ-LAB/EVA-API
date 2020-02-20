ALTER TABLE `log_biz`
	ADD COLUMN `device` varchar(60) COMMENT '设备类型'  NULL,
	ADD COLUMN `version` varchar(15) COMMENT '应用版本'  NULL
;
