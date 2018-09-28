CREATE TABLE `log_biz`  (
	`ID`             	varchar(40) NOT NULL,
	`operator`       	varchar(10) NULL,
	`operate_type`    	varchar(10) NULL,
	`operate_dateTime`	varchar(20) NULL,
	`description`    	varchar(300) NULL,
	PRIMARY KEY(`ID`)
)
