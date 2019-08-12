/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.10.166-Percona-ROOT
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : 192.168.10.166:12306
 Source Schema         : eva

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : 65001

 Date: 11/08/2019 22:59:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for log_biz
-- ----------------------------
DROP TABLE IF EXISTS `log_biz`;
CREATE TABLE `log_biz`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `operator` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operate_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operate_dateTime` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典编码',
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典描述',
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????ID',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT 1,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES ('1', 'biz', '业务代码', '0', '业务代码', '1899-12-31 08:00:00', '1899-12-31 08:00:00', NULL, '', 1);
INSERT INTO `sys_dict` VALUES ('3', 'sys', '系统代码', '0', 'fdvdfv', '1899-12-31 08:00:00', '1899-12-31 08:00:00', NULL, NULL, 1);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MAIN_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `key_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典键名',
  `key_value` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典键值',
  `orders` int(11) NULL DEFAULT NULL COMMENT '排序',
  `status` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_module`;
CREATE TABLE `sys_module`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `icon` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `routeUrl` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `modelUrl` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前端model url',
  `PATH_ID` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径ID',
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点ID',
  `PARENT_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点名称',
  `path` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问路径',
  `PATH_NAME` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径描述',
  `isleaf` tinyint(4) NULL DEFAULT NULL COMMENT '是否叶子节点',
  `orders` int(11) NULL DEFAULT NULL COMMENT '排序',
  `status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_module
-- ----------------------------
INSERT INTO `sys_module` VALUES ('10', '字典管理', 'profile', NULL, NULL, '5,5', '5', '系统管理', '/sys/dictionary', NULL, 0, 3, '0001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_module` VALUES ('5', '系统管理', 'setting', NULL, NULL, '5', NULL, '系统管理', '/sys', NULL, 0, 0, '0001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_module` VALUES ('6', '组织管理', 'flag', NULL, NULL, '5,5', '5', '系统管理', '/sys/organization', NULL, 0, 1, '0001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_module` VALUES ('7', '模块管理', 'bars', NULL, NULL, '5,5', '5', '系统管理', '/sys/module', NULL, 0, 0, '0001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_module` VALUES ('8', '用户管理', 'usergroup-add', NULL, NULL, '5,5', '5', '系统管理', '/sys/account', NULL, 0, 4, '0001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_module` VALUES ('9', '权限管理', 'form', NULL, NULL, '5,5', '5', '系统管理', '/sys/role', NULL, 0, 2, '0001', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_organization
-- ----------------------------
DROP TABLE IF EXISTS `sys_organization`;
CREATE TABLE `sys_organization`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `PARENT_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `path` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `PATH_NAME` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `isleaf` tinyint(4) NULL DEFAULT NULL,
  `orders` int(11) NULL DEFAULT NULL,
  `status` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '逻辑删除',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `deleted` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0001',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_organization
-- ----------------------------
INSERT INTO `sys_organization` VALUES ('1', '统合部', 'THB', '0', NULL, '1', '统合部', 1, 1, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('10', '加达里后勤部', 'GHQ', '6', '加达里合众国', '6/10', '加达里合众国/加达里后勤部', 0, 2, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('11', '加达里钢铁集团', 'GGT', '6', '加达里合众国', '6/11', '加达里合众国/加达里钢铁集团', 0, 3, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('12', '卡拉吉塔集团', 'GKL', '6', '加达里合众国', '6/12', '加达里合众国/卡拉吉塔集团', 0, 1, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('13', '应用知识学院', 'GKN', '6', '加达里合众国', '6/13', '加达里合众国/应用知识学院', 0, 4, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('14', '古斯塔斯', 'GSTS', '0', NULL, '14', '古斯塔斯', 0, 2, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('15', '古斯塔斯制造', 'GSZZ', '14', '古斯塔斯', '14/15', '古斯塔斯/古斯塔斯制造', 0, 1, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('2', '协约理事会', 'LSH', '1', '统合部', '1/2', '统合部/协约理事会', 0, 3, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('3', '联合安全局', 'DED', '1', '统合部', '1/3', '统合部/联合安全局', 0, 1, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('4', '商业安全委员会', 'CAD', '1', '统合部', '1/4', '统合部/商业安全委员会', 0, 0, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('5', '统合关系司令部', 'SLB', '1', '统合部', '1/5', '统合部/统合关系司令部', 0, 2, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('6', '加达里合众国', 'GDRR', '0', NULL, '6', '加达里合众国', 0, 0, '0001', NULL, NULL, NULL, NULL, NULL, '0001');
INSERT INTO `sys_organization` VALUES ('7', '三岛集团', 'SD', '6', '加达里合众国', '6/7', '加达里合众国/三岛集团', 0, 0, '0001', NULL, NULL, NULL, NULL, NULL, '0001');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `CODE` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色编码',
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点ID',
  `PARENT_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点名称',
  `PATH` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径',
  `PATH_NAME` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径描述',
  `ISLEAF` tinyint(4) NULL DEFAULT NULL COMMENT '是否叶子',
  `ORDERS` int(11) NULL DEFAULT NULL COMMENT '排序',
  `LOCKED` tinyint(4) NULL DEFAULT 0,
  `REMARK` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `GMT_CREATE` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `GMT_MODIFY` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES ('1', '系统管理员', 'ROLE_ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_module`;
CREATE TABLE `sys_role_module`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ROLE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色ID',
  `MODULE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块ID',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_module
-- ----------------------------
INSERT INTO `sys_role_module` VALUES ('623df7e29e9cc1ccfdafb4013a406066', '1', '8');
INSERT INTO `sys_role_module` VALUES ('63808bcc6383d5daa5470195d7043472', '1', '7');
INSERT INTO `sys_role_module` VALUES ('9154eaf32a1c8551a97f18186d733073', '1', '10');
INSERT INTO `sys_role_module` VALUES ('93fcbe4caf5794a1e6a737d5535c10c7', '1', '5');
INSERT INTO `sys_role_module` VALUES ('ac070a22ae2b5b4d29c9460622e385fe', '1', '6');
INSERT INTO `sys_role_module` VALUES ('d2ac85e592f8ba4b3aa0261901d78aad', '1', '9');

-- ----------------------------
-- Table structure for sys_role_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_user`;
CREATE TABLE `sys_role_user`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ROLE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `USER_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_user
-- ----------------------------
INSERT INTO `sys_role_user` VALUES ('dec21313079d77df75f8b2551843319f', '1', '9199482d76b443ef9f13fefddcf0046c');

-- ----------------------------
-- Table structure for sys_user_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_info`;
CREATE TABLE `sys_user_info`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `account` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `salt` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `nick_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `register_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_register` datetime(0) NULL DEFAULT NULL,
  `last_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `last_login` datetime(0) NULL DEFAULT NULL,
  `locked` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dept_id` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `deleted` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0001' COMMENT '逻辑删除',
  `dept_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_info
-- ----------------------------
INSERT INTO `sys_user_info` VALUES ('9199482d76b443ef9f13fefddcf0046c', 'admin', 'admin', '$2a$10$DyoRv31eET5SuMaF7gH/4u0bfY7UQ5FfGw21aTMkjz9hhCZYuZPeu', 'i4z62k6qcaxspaqa', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '超级管理员', NULL, NULL, NULL, NULL, NULL, '0000', NULL, NULL, NULL, NULL, '1', NULL, 'admin', NULL, '0001', '统合部');

-- ----------------------------
-- View structure for v_dict
-- ----------------------------
DROP VIEW IF EXISTS `v_dict`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_dict` AS select `a`.`code` AS `code`,`a`.`name` AS `name`,`b`.`key_name` AS `key_name`,`b`.`key_value` AS `key_value`,`b`.`orders` AS `orders` from (`sys_dict` `a` join `sys_dict_item` `b` on(((`a`.`ID` = `b`.`MAIN_ID`) and (`b`.`status` = 1)))) where ((`a`.`status` = 1) and (`a`.`PARENT_ID` <> '0') and (`a`.`PARENT_ID` is not null)) order by `a`.`code`,`b`.`orders`;

-- ----------------------------
-- View structure for v_granted_module
-- ----------------------------
DROP VIEW IF EXISTS `v_granted_module`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_granted_module` AS select `m`.`ID` AS `ID`,`m`.`name` AS `name`,`m`.`icon` AS `icon`,`m`.`routeUrl` AS `routeUrl`,`m`.`modelUrl` AS `modelUrl`,`m`.`PATH_ID` AS `PATH_ID`,`m`.`PARENT_ID` AS `PARENT_ID`,`m`.`PARENT_NAME` AS `PARENT_NAME`,`m`.`path` AS `path`,`m`.`PATH_NAME` AS `PATH_NAME`,`m`.`isleaf` AS `isleaf`,`m`.`orders` AS `orders`,`m`.`status` AS `status`,`m`.`remark` AS `remark`,`m`.`gmt_create` AS `gmt_create`,`m`.`gmt_modify` AS `gmt_modify`,`m`.`create_by` AS `create_by`,`m`.`modify_by` AS `modify_by`,`sr`.`CODE` AS `role_Code`,`sr`.`ID` AS `role_Id` from ((`sys_module` `m` join `sys_role_module` `r`) join `sys_role` `sr`) where ((`m`.`ID` = `r`.`MODULE_ID`) and (`r`.`ROLE_ID` = `sr`.`ID`));

SET FOREIGN_KEY_CHECKS = 1;
