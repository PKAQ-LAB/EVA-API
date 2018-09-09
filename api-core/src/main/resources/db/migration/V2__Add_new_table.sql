DROP TABLE IF EXISTS flyway_test_alpha;
CREATE TABLE `flyway_test_alpha`  (
	`id`          	int(11) AUTO_INCREMENT NOT NULL,
	`column_alpha`	varchar(30) NULL,
	PRIMARY KEY(`id`)
)
