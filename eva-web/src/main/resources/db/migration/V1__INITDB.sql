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

 Date: 27/11/2019 14:57:11
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
  `method` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `class_name` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `params` varchar(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `response` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备类型',
  `version` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用版本',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for log_error
-- ----------------------------
DROP TABLE IF EXISTS `log_error`;
CREATE TABLE `log_error`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `REQUEST_TIME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '记录时间',
  `IP` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求ip',
  `SPEND_TIME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求耗时',
  `CLASS_NAME` varchar(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类名',
  `METHOD` varchar(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法名',
  `PARAMS` varchar(6000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法参数',
  `EX_DESC` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常描述',
  `LOGIN_USER` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录用户',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日志监控_异常日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典编码',
  `name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典描述',
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上级ID',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES ('1', 'biz', '业务代码', '0', '业务代码', '1899-12-31 08:00:00', '1899-12-31 08:00:00', NULL, '', '9999');
INSERT INTO `sys_dict` VALUES ('1174974634025496578', 'data_permission', '数据权限', '1', '数据权限类型', '2019-09-20 17:12:37', '2019-09-20 19:41:56', NULL, NULL, '9999');
INSERT INTO `sys_dict` VALUES ('3', 'sys', '系统代码', '0', '系统代码', '1899-12-31 08:00:00', '1899-12-31 08:00:00', NULL, NULL, '9999');
INSERT INTO sys_dict  ( id, code, name, parent_id, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1247704849712906241', 'dict_type', '所属分类', '0001', '9199482d76b443ef9f13fefddcf0046c', '2020-04-08T09:56:32.070004400', '9199482d76b443ef9f13fefddcf0046c', '2020-04-08T09:56:32.070004400' );

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
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES ('1174974634100994050', '1174974634025496578', '0000', '全部', 0);
INSERT INTO `sys_dict_item` VALUES ('1174974634117771265', '1174974634025496578', '0001', '仅本部门', 1);
INSERT INTO `sys_dict_item` VALUES ('1174974634142937089', '1174974634025496578', '0002', '本部门及下属部门', 2);
INSERT INTO `sys_dict_item` VALUES ('1174974634159714305', '1174974634025496578', '0003', '指定部门', 3);
INSERT INTO `sys_dict_item` VALUES ('1174974634176491521', '1174974634025496578', '0005', '仅本人创建', 5);
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '5ee97d5c83464aeaa08dc441c396f62b', '1247704849712906241', '0001', '系统代码' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( 'f6fb18fd71c665baf57e680bc5a28fec', '1247704849712906241', '0002', '业务代码' );

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
  `create_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_module
-- ----------------------------
INSERT INTO `sys_module` VALUES ('10', '字典管理', 'profile', NULL, NULL, '5,5', '5', NULL, '/sys/dictionary', NULL, 1, 4, '9999', NULL, NULL, '2019-11-01 10:36:40', NULL, '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('1186192719776305153', '系统监控', 'radar-chart', NULL, NULL, NULL, NULL, NULL, '/monitor', NULL, 1, 5, '9999', NULL, '2019-10-21 16:09:17', '2019-10-21 16:09:17', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('1186192847761297410', '业务日志', 'file', NULL, NULL, '1186192719776305153', '1186192719776305153', '系统监控', '/monitor/log/biz', '系统监控/业务日志', 1, 1, '9999', NULL, '2019-10-21 16:09:48', '2019-10-21 16:11:02', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('1186192979059789826', '异常日志', 'file', NULL, NULL, '1186192719776305153', '1186192719776305153', '系统监控', '/monitor/log/error', '系统监控/异常日志', 1, 2, '9999', NULL, '2019-10-21 16:10:19', '2019-10-21 16:11:08', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('1187588287316574209', '代码生成', 'rocket', NULL, NULL, NULL, NULL, NULL, '/dev/generator', NULL, 1, 6, '9999', NULL, '2019-10-25 12:34:47', '2019-10-25 12:34:47', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('1187919802302926850', '进销存', 'home', NULL, NULL, NULL, NULL, NULL, '/pdos', NULL, 1, 7, '9999', NULL, '2019-10-26 10:32:06', '2019-10-26 10:32:06', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('5', '系统管理', 'setting', NULL, NULL, '5', NULL, '系统管理', '/sys', NULL, 0, 0, '9999', NULL, NULL, '2019-10-15 12:41:52', NULL, NULL);
INSERT INTO `sys_module` VALUES ('6', '组织管理', 'flag', NULL, NULL, '5,5', '5', NULL, '/sys/organization', NULL, 1, 0, '9999', NULL, NULL, '2019-11-01 10:36:52', NULL, '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('7', '模块管理', 'bars', NULL, NULL, '5,5', '5', NULL, '/sys/module', NULL, 1, 3, '9999', NULL, NULL, '2019-11-01 10:36:56', NULL, '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('8', '用户管理', 'usergroup-add', NULL, NULL, '5,5', '5', NULL, '/sys/account', NULL, 1, 1, '9999', NULL, NULL, '2019-11-01 10:36:55', NULL, '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_module` VALUES ('9', '权限管理', 'form', NULL, NULL, '5,5', '5', NULL, '/sys/role', NULL, 1, 2, '9999', NULL, NULL, '2019-11-01 10:36:56', NULL, '9199482d76b443ef9f13fefddcf0046c');

-- ----------------------------
-- Table structure for sys_module_resources
-- ----------------------------
DROP TABLE IF EXISTS `sys_module_resources`;
CREATE TABLE `sys_module_resources`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `MODULE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块ID',
  `RESOURCE_DESC` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源描述',
  `RESOURCE_URL` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源路径',
  `RESOURCE_TYPE` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源类型',
  PRIMARY KEY (`ID`) USING BTREE,
  INDEX `FK_REFERENCE_6`(`MODULE_ID`) USING BTREE,
  INDEX `ID`(`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '基础管理_模块管理_模块资源' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_module_resources
-- ----------------------------
INSERT INTO `sys_module_resources` VALUES ('10', '10', '全部资源', '**', '');
INSERT INTO `sys_module_resources` VALUES ('1186193037775851522', '1186192979059789826', '全部资源', '/**', '9999');
INSERT INTO `sys_module_resources` VALUES ('1186193072919924737', '1186192847761297410', '全部资源', '/**', '9999');
INSERT INTO `sys_module_resources` VALUES ('1187931885706899458', '1187931885673345025', '全部资源', '/**', '9999');
INSERT INTO `sys_module_resources` VALUES ('1187931978862391298', '1187931978845614082', '全部资源', '/**', '9999');
INSERT INTO `sys_module_resources` VALUES ('1187934072734441473', '1187934072596029442', '全部资源', '/**', '9999');

INSERT INTO `sys_module_resources` VALUES ('6', '6', '全部资源', '/**', '');
INSERT INTO `sys_module_resources` VALUES ('7', '7', '全部资源', '**', '');
INSERT INTO `sys_module_resources` VALUES ('8', '8', '全部资源', '**', '');
INSERT INTO `sys_module_resources` VALUES ('9', '9', '全部资源', '**', '');

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
  `create_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `deleted` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0000',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_organization
-- ----------------------------
INSERT INTO `sys_organization` VALUES ('1', '统合部', 'THB', '0', NULL, '1', '统合部', 1, 1, '9999', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('12', '卡拉吉塔集团', 'GKL', '6', '加达里', '6/12', '加达里/卡拉吉塔集团', 0, 1, '0000', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('13', '应用知识学院', 'GKN', '6', '加达里', '6/13', '加达里/应用知识学院', 0, 4, '0000', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('2', '协约理事会', 'LSH', '1', '统合部', '1/2', '统合部/协约理事会', 0, 3, '9999', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('3', '联合安全局', 'DED', '1', '统合部', '1/3', '统合部/联合安全局', 0, 1, '9999', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('4', '商业安全委员会', 'CAD', '1', '统合部', '1/4', '统合部/商业安全委员会', 0, 0, '9999', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('5', '统合关系司令部', 'SLB', '1', '统合部', '1/5', '统合部/统合关系司令部', 0, 2, '9999', NULL, NULL, NULL, NULL, NULL, '0000');
INSERT INTO `sys_organization` VALUES ('6', '加达里', 'GDRR', '0', NULL, '6', '加达里', 0, 0, '0000', NULL, NULL, '2019-10-28 20:24:22', NULL, '9199482d76b443ef9f13fefddcf0046c', '0000');
INSERT INTO `sys_organization` VALUES ('7', '三岛集团', 'SD', '6', '加达里', '6/7', '加达里/三岛集团', 0, 0, '0000', NULL, NULL, NULL, NULL, NULL, '0000');

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
  `LOCKED` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0000',
  `REMARK` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `GMT_CREATE` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `GMT_MODIFY` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `DATA_PERMISSION_TYPE` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据权限类型',
  `DATA_PERMISSION_DEPTID` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限部门ID',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES ('1', '系统管理员', 'ROLE_ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, '9999', NULL, NULL, NULL, NULL, NULL, '0000', NULL);
INSERT INTO `sys_role` VALUES ('1191916813788344321', '系统操作员', 'ROLE_OPERATOR', NULL, NULL, NULL, NULL, NULL, NULL, '0000', '系统操作员 仅可操作本人所属部门及下级部门数据', '2019-11-06 11:14:48', '2019-11-06 14:36:19', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c', '0002', NULL);

-- ----------------------------
-- Table structure for sys_role_configuration
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_configuration`;
CREATE TABLE `sys_role_configuration`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ROLE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色ID',
  `PARAM_KEY` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数编码',
  `PARAM_VAL` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数值',
  PRIMARY KEY (`ID`) USING BTREE,
  INDEX `FK_SYS_ROLE_CONFIGURATION`(`ROLE_ID`) USING BTREE,
  CONSTRAINT `FK_SYS_ROLE_CONFIGURATION` FOREIGN KEY (`ROLE_ID`) REFERENCES `sys_role` (`ID`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_module`;
CREATE TABLE `sys_role_module`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ROLE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色ID',
  `MODULE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块ID',
  `RESOURCE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源ID',
  PRIMARY KEY (`ID`) USING BTREE,
  INDEX `fk_rmr`(`RESOURCE_ID`) USING BTREE,
  CONSTRAINT `fk_rmr` FOREIGN KEY (`RESOURCE_ID`) REFERENCES `sys_module_resources` (`ID`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_module
-- ----------------------------
INSERT INTO `sys_role_module` VALUES ('11', '2', '11', NULL);
INSERT INTO `sys_role_module` VALUES ('1187937381595709441', '1', '1186192979059789826', '1186193037775851522');
INSERT INTO `sys_role_module` VALUES ('1187937381608292353', '1', '6', '6');
INSERT INTO `sys_role_module` VALUES ('1187937381625069569', '1', '7', '7');
INSERT INTO `sys_role_module` VALUES ('1187937381637652481', '1', '8', '8');
INSERT INTO `sys_role_module` VALUES ('1187937381654429697', '1', '1186192719776305153', NULL);
INSERT INTO `sys_role_module` VALUES ('1187937381671206913', '1', '1187588287316574209', NULL);
INSERT INTO `sys_role_module` VALUES ('1187937381687984129', '1', '9', '9');
INSERT INTO `sys_role_module` VALUES ('1187937381704761346', '1', '1187919802302926850', NULL);
INSERT INTO `sys_role_module` VALUES ('1187937381721538562', '1', '1186192847761297410', '1186193072919924737');
INSERT INTO `sys_role_module` VALUES ('1187937381738315777', '1', '10', '10');
INSERT INTO `sys_role_module` VALUES ('1187937381750898690', '1', '5', NULL);

INSERT INTO `sys_role_module` VALUES ('1191921989085532161', '1191916813788344321', '1186192979059789826', '1186193037775851522');
INSERT INTO `sys_role_module` VALUES ('1191921989106503681', '1191916813788344321', '6', '6');
INSERT INTO `sys_role_module` VALUES ('1191921989114892289', '1191916813788344321', '7', '7');
INSERT INTO `sys_role_module` VALUES ('1191921989131669506', '1191916813788344321', '8', '8');
INSERT INTO `sys_role_module` VALUES ('1191921989148446722', '1191916813788344321', '1186192719776305153', NULL);
INSERT INTO `sys_role_module` VALUES ('1191921989156835329', '1191916813788344321', '9', '9');
INSERT INTO `sys_role_module` VALUES ('1191921989173612546', '1191916813788344321', '1186192847761297410', '1186193072919924737');
INSERT INTO `sys_role_module` VALUES ('1191921989182001154', '1191916813788344321', '10', '10');
INSERT INTO `sys_role_module` VALUES ('1191921989198778370', '1191916813788344321', '5', NULL);

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
INSERT INTO `sys_role_user` VALUES ('1183314144530264065', '1', '9199482d76b443ef9f13fefddcf0046c');
INSERT INTO `sys_role_user` VALUES ('1184376663802040322', '1', '1173117564929937409');
INSERT INTO `sys_role_user` VALUES ('1184377281077760002', '1', '1184377281048399874');
INSERT INTO `sys_role_user` VALUES ('1184382031080902658', '1', '1184382031034765313');
INSERT INTO `sys_role_user` VALUES ('1184390438361739266', '1', '1184390438344962049');
INSERT INTO `sys_role_user` VALUES ('1192011503128739842', '1191916813788344321', '1190104332380127233');

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
  `tel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `register_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_register` datetime(0) NULL DEFAULT NULL,
  `last_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `last_login` datetime(0) NULL DEFAULT NULL,
  `locked` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modify` datetime(0) NULL DEFAULT NULL,
  `create_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `modify_by` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dept_id` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dept_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `deleted` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0000' COMMENT '逻辑删除',
   `weixin_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信openid',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_info
-- ----------------------------
INSERT INTO `sys_user_info` VALUES ('1190104332380127233', NULL, 'scott', '{bcrypt}$2a$10$hxJDzKrEBPaYfpYne/.07OEjoK8s5vYv9HpC6Si73UzFj6Pg5om/6', NULL, '', '操作员',  NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0000', '2019-11-01 11:12:39', '2019-11-06 17:31:04', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c', '6','加达里', NULL, '0000', '');
INSERT INTO `sys_user_info` VALUES ('9199482d76b443ef9f13fefddcf0046c', 'admin', 'admin', '{bcrypt}$2a$10$Nv1jaTk1xeCA7Mmzn9jcBeaOP5DjW5X9DW.9bu02Hs6/cOdbyMIF.', 'i4z62k6qcaxspaqa', '', '超级管理员', NULL, 'pkaq@msn.com', '133', NULL, NULL, NULL, NULL, '9999', NULL, '2019-10-09 20:20:04', NULL, NULL, '1','统合部', 'AAACCC', '0000', '');

-- ----------------------------
-- View structure for v_dict
-- ----------------------------
DROP VIEW IF EXISTS `v_dict`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_dict` AS select `a`.`code` AS `code`,`a`.`name` AS `name`,`b`.`key_name` AS `key_name`,`b`.`key_value` AS `key_value`,`b`.`orders` AS `orders` from (`sys_dict` `a` join `sys_dict_item` `b` on(((`a`.`ID` = `b`.`MAIN_ID`) and (`a`.`status` <> '0000')))) where ((`a`.`status` <> '0000') and (`a`.`PARENT_ID` <> '0') and (`a`.`PARENT_ID` is not null)) order by `a`.`code`,`b`.`orders`;

-- ----------------------------
-- View structure for v_granted_module
-- ----------------------------
DROP VIEW IF EXISTS `v_granted_module`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_granted_module` AS select `m`.`ID` AS `ID`,`m`.`name` AS `name`,`m`.`icon` AS `icon`,`m`.`routeUrl` AS `routeUrl`,`m`.`modelUrl` AS `modelUrl`,`m`.`PATH_ID` AS `PATH_ID`,`m`.`PARENT_ID` AS `PARENT_ID`,`m`.`PARENT_NAME` AS `PARENT_NAME`,`m`.`path` AS `path`,`m`.`PATH_NAME` AS `PATH_NAME`,`m`.`isleaf` AS `isleaf`,`m`.`orders` AS `orders`,`m`.`status` AS `status`,`m`.`remark` AS `remark`,`m`.`gmt_create` AS `gmt_create`,`m`.`gmt_modify` AS `gmt_modify`,`m`.`create_by` AS `create_by`,`m`.`modify_by` AS `modify_by`,`sr`.`CODE` AS `role_Code`,`sr`.`ID` AS `role_Id` from ((`sys_module` `m` join `sys_role_module` `r`) join `sys_role` `sr`) where ((`m`.`ID` = `r`.`MODULE_ID`) and (`r`.`ROLE_ID` = `sr`.`ID`));

SET FOREIGN_KEY_CHECKS = 1;
