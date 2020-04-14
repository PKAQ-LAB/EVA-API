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

 Date: 06/11/2019 18:17:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- 初始化菜单及权限
-- ----------------------------
INSERT INTO sys_module  ( id, name, icon, orders, status, parent_id, parent_name, path, path_id, path_name, isleaf, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1248081570614149122', '线上销售单', 'snippets', 2, '0000', '1187919802302926850', '进销存', '/pdos/sale/slip', '1187919802302926850', '进销存/线上销售单', true, '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_module_resources  ( id, module_id, resource_desc, resource_url, resource_type )  VALUES  ( '1248081570945499138', '1248081570614149122', '全部资源', '/**', '9999' );
INSERT INTO sys_role_module  ( id, role_id, module_id, resource_id )  VALUES  ( '1248083168849494018', '1', '1248081570614149122', '1248081570945499138' );

INSERT INTO sys_module  ( id, name, icon, orders, status, parent_id, parent_name, path, path_id, path_name, isleaf, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1249176398601330690', '基础管理', 'lock', 3, '0000', '1187919802302926850', '进销存', '/pdos/base', '1187919802302926850', '进销存/基础管理', true, '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_module_resources  ( id, module_id, resource_desc, resource_url, resource_type )  VALUES  ( '1249176400157417474', '1249176398601330690', '全部资源', '/**', '9999' );
INSERT INTO sys_role_module  ( id, role_id, module_id, resource_id )  VALUES  ( '1249177176938323969', '1', '1187936839427391490', '1187936839473528833' );

INSERT INTO sys_module  ( id, name, icon, orders, status, parent_id, parent_name, path, path_id, path_name, isleaf, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1249178962021523457', '商品类目', 'bars', 1, '0000', '1249176398601330690', '基础管理', '/pdos/base/category', '1187919802302926850,1249176398601330690', '基础管理/商品类目', true, '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_module_resources  ( id, module_id, resource_desc, resource_url, resource_type )  VALUES  ( '1249178963804102658', '1249178962021523457', '全部资源', '/**', '9999' );
INSERT INTO sys_role_module  ( id, role_id, module_id, resource_id )  VALUES  ( '1249179294533361666', '1', '1249178962021523457', '1249178963804102658' );

INSERT INTO sys_module  ( id, name, icon, orders, status, parent_id, parent_name, path, path_id, path_name, isleaf, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '18101', '供应商管理', 'bars', 1, '0000', '1249176398601330690', '基础管理', '/pdos/base/supplier', '1187919802302926850,1249176398601330690', '基础管理/供应商管理', true, '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_module_resources  ( id, module_id, resource_desc, resource_url, resource_type )  VALUES  ( '28201', '18101', '全部资源', '/**', '9999' );
INSERT INTO sys_role_module  ( id, role_id, module_id, resource_id )  VALUES  ( '38301', '1', '18101', '28201' );

INSERT INTO sys_module  ( id, name, icon, orders, status, parent_id, parent_name, path, path_id, path_name, isleaf, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '18102', '产品管理', 'bars', 1, '0000', '1249176398601330690', '基础管理', '/pdos/base/goods', '1187919802302926850,1249176398601330690', '基础管理/产品管理', true, '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_module_resources  ( id, module_id, resource_desc, resource_url, resource_type )  VALUES  ( '28202', '18102', '全部资源', '/**', '9999' );
INSERT INTO sys_role_module  ( id, role_id, module_id, resource_id )  VALUES  ( '38302', '1', '18102', '28202' );
-- ----------------------------
-- 初始化字典
-- ----------------------------
INSERT INTO sys_dict  ( id, code, name, parent_id, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1247706210710028290', 'online_platform', '来源平台', '1', '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( 'a65ac783e9b93d86fa862bc238e2c83e', '1247706210710028290', '0001', '拼多多' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( 'c370d1ba2e580a97d60741055170cab4', '1247706210710028290', '0002', '淘宝' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( 'c370d1ba2e580a97d60741055170cac6', '1247706210710028290', '0003', '闲鱼' );

INSERT INTO sys_dict  ( id, code, name, parent_id, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1247707145754607617', 'ship_company', '快递公司', '1', '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '5e800ffc87bd97f11253931d03dd00a8', '1247707145754607617', '0001', '圆通快递' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '147be54e535f46917fa4d90c3ffba613', '1247707145754607617', '0002', '中通快递' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '708ae86549fbe78b483f7fd1ab28cd6b', '1247707145754607617', '0003', '京东快递' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '60ed545f6803b47e161e67959a811504', '1247707145754607617', '0004', '顺丰快递' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '60ed545f6803b47e161e67959a811516', '1247707145754607617', '0005', '百世快递' );

INSERT INTO `sys_dict` VALUES ('1187716348942970881', 'purchasing_type', '采购类型', '1', NULL, '2019-10-25 21:03:39', '2019-10-25 21:17:06', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c', '1');
INSERT INTO `sys_dict_item` VALUES ('1187717107797413889', '1187716348942970881', '0001', '网络采购', NULL);
INSERT INTO `sys_dict_item` VALUES ('1187719735684030466', '1187716348942970881', '0002', '市场采购', NULL);

INSERT INTO `sys_dict` VALUES ('1187908136467095553', 'goods_type', '货品类型', '1', NULL, '2019-10-26 09:45:45', '2019-10-31 13:12:06', '9199482d76b443ef9f13fefddcf0046c', '9199482d76b443ef9f13fefddcf0046c', '1');
INSERT INTO `sys_dict_item` VALUES ('1187908136500649986', '1187908136467095553', '0001', '玩具', NULL);
INSERT INTO `sys_dict_item` VALUES ('1187908136525815809', '1187908136467095553', '0002', '饰品', NULL);

INSERT INTO sys_dict  ( id, code, name, parent_id, create_by, gmt_create, modify_by, gmt_modify )  VALUES  ( '1247706210710028291', 'supplier_type', '供应商类型', '1', '9199482d76b443ef9f13fefddcf0046c', null, '9199482d76b443ef9f13fefddcf0046c', null );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '12477062107100282911', '1247706210710028291', '0001', '厂家' );
INSERT INTO sys_dict_item  ( id, main_id, key_name, key_value )  VALUES  ( '12477062107100282912', '1247706210710028291', '0002', '经销商' );
-- ----------------------------
-- Table structure for jxc_base_category
-- ----------------------------
DROP TABLE IF EXISTS `jxc_base_category`;
CREATE TABLE `jxc_base_category`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `CODE` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类编码',
  `NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类名称',
  `PARENT_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点ID',
  `PARENT_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父节点名称',
  `PATH` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径',
  `PATH_ID` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径ID',
  `PATH_NAME` varchar(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路径描述',
  `ISLEAF` tinyint(4) NULL DEFAULT NULL COMMENT '是否叶子',
  `ORDERS` int(11) NULL DEFAULT NULL COMMENT '排序',
  `STATUS` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `REMARK` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `GMT_CREATE` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `GMT_MODIFY` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;


/*==============================================================*/
/* Table: JXC_BASE_GOODS                                        */
/*==============================================================*/
DROP TABLE IF EXISTS JXC_BASE_GOODS;
CREATE TABLE JXC_BASE_GOODS
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   NAME                 VARCHAR(40) COMMENT '品名',
   MNEMONIC             VARCHAR(12) COMMENT '助记码（拼音）',
   CATEGORY             VARCHAR(40) COMMENT '品类',
   AVATAR               VARCHAR(300) COMMENT '图片',
   ITEM_NO              VARCHAR(20) COMMENT '货号',
   BARCODE              VARCHAR(60) COMMENT '条码',
   UNIT                 VARCHAR(4) COMMENT '规格单位',
   BOXUNIT              INT COMMENT '装箱规格',
   FACTORY              VARCHAR(40) COMMENT '生产厂家',
   REMARK               VARCHAR(300) COMMENT '备注',
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

-- ----------------------------
-- Table structure for jxc_base_suppliiers
-- ----------------------------
DROP TABLE IF EXISTS `jxc_base_suppliiers`;
CREATE TABLE `jxc_base_suppliiers`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `FULL_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '全称',
  `NAME` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简称',
  `MNEMONIC` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '助记码',
  `CATEGORY` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型(0001 生产厂家，0002 代理商)',
  `LINKMAN` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `MOBILE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `ADDRESS` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `STATUS` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `REMARK` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `GMT_CREATE` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `GMT_MODIFY` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for jxc_cashjournal
-- ----------------------------
DROP TABLE IF EXISTS `jxc_cashjournal`;
CREATE TABLE `jxc_cashjournal`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `YEAR` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `MONTH` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `DAY` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `CATEGORY` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `NUMS` int(11) NULL DEFAULT NULL,
  `PRICE` decimal(12, 4) NULL DEFAULT NULL,
  `TOTAL` decimal(12, 4) NULL DEFAULT NULL,
  `REMARK` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_CREATE` date NULL DEFAULT NULL,
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_MODIFY` date NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for jxc_purchasing_line
-- ----------------------------
DROP TABLE IF EXISTS `jxc_purchasing_line`;
CREATE TABLE `jxc_purchasing_line`  (
  `LINE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'LINE_ID',
  `MAIN_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GOODS_ID` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品ID',
  `NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品名',
  `CATEGORY` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品类',
  `MODEL` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品型号',
  `BARCODE` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条码',
  `UNIT` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格单位',
  `MNEMONIC` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '助记码（拼音）',
  `NUM` decimal(12, 4) NULL DEFAULT NULL COMMENT '进货数量',
  `VAT` decimal(12, 4) NULL DEFAULT NULL COMMENT '含税单价',
  `VAT_AMOUNT` decimal(12, 4) NULL DEFAULT NULL COMMENT '含税金额',
  `TAX_RATE` decimal(12, 4) NULL DEFAULT NULL COMMENT '税率',
  `TAX_AMOUNT` decimal(12, 4) NULL DEFAULT NULL COMMENT '税额',
  `NOTAX_VALUE` decimal(12, 4) NULL DEFAULT NULL COMMENT '不含税单价',
  `NOTAX_AMOUNT` decimal(12, 4) NULL DEFAULT NULL COMMENT '不含税金额',
  `STOCK_NUM` decimal(12, 4) NULL DEFAULT NULL COMMENT '库存数量',
  `STOCK_AMOUNT` decimal(12, 4) NULL DEFAULT NULL COMMENT '库存金额',
  PRIMARY KEY (`LINE_ID`) USING BTREE,
  INDEX `FK_FK_INSTOCK`(`MAIN_ID`) USING BTREE,
  CONSTRAINT `FK_FK_INSTOCK` FOREIGN KEY (`MAIN_ID`) REFERENCES `jxc_purchasing_order` (`ID`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Records of jxc_purchasing_line
-- ----------------------------
BEGIN;
INSERT INTO `jxc_purchasing_line` VALUES ('1188026062882344960', '1188026062869762048', NULL, 'A', NULL, 'A1', 'A1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), ('1188026062882344961', '1188026062869762048', NULL, 'B', NULL, 'B2', 'B2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), ('1188698027846668288', '1187994936495181824', NULL, 'B', NULL, 'B2', 'B2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), ('1188698027884417024', '1187994936495181824', NULL, 'A', NULL, 'A1', 'A1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for jxc_purchasing_order
-- ----------------------------
DROP TABLE IF EXISTS `jxc_purchasing_order`;
CREATE TABLE `jxc_purchasing_order`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `CODE` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '入库单号',
  `ORDER_DATE` date NULL DEFAULT NULL COMMENT '入库日期',
  `STOCK` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仓库',
  `PURCHASING_TYPE` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采购类型',
  `OPERATOR` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '制单人',
  `OPERATOR_NM` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '制单人名称',
  `PURCHASER` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采购人',
  `PURCHASER_NM` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采购人名称',
  `SUPPLIER_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商',
  `SUPPLIER_NM` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `REMARK` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_CREATE` date NULL DEFAULT NULL,
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_MODIFY` date NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '采购入库单';

-- ----------------------------
-- Records of jxc_purchasing_order
-- ----------------------------
BEGIN;
INSERT INTO `jxc_purchasing_order` VALUES ('1187994936495181824', 'AA333CCC', '2019-10-03', '0002', NULL, NULL, 'AAA', NULL, 'AAA', NULL, 'AA', NULL, NULL, NULL, NULL, '2019-10-28'), ('1188026062869762048', 'aaa', '2019-10-19', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2019-10-26', NULL, '2019-10-26');
COMMIT;

-- ----------------------------
-- Table structure for jxc_retailsale
-- ----------------------------
DROP TABLE IF EXISTS `jxc_retailsale`;
CREATE TABLE `jxc_retailsale`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `GOODS_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ORDER_CODE` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `NUMMER` decimal(14, 4) NULL DEFAULT NULL,
  `PRICE` decimal(14, 4) NULL DEFAULT NULL,
  `TOTAL` decimal(14, 4) NULL DEFAULT NULL,
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_CREATE` date NULL DEFAULT NULL,
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_MODIFY` date NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for jxc_stock
-- ----------------------------
DROP TABLE IF EXISTS `jxc_stock`;
CREATE TABLE `jxc_stock`  (
  `ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `GOODS_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品ID',
  `MNEMONIC` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '助记码（拼音）',
  `BARCODE` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条码',
  `MODEL` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品型号',
  `NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品名',
  `CATEGORY` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品类',
  `UNIT_PRICE` decimal(12, 2) NULL DEFAULT NULL COMMENT '单价',
  `UNIT` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格单位',
  `STORAGE_ID` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '存放仓库',
  `ALLOCATION` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '货位',
  `ONHAND` int(11) NULL DEFAULT NULL COMMENT '库存数量',
  `AVAILABLE` int(11) NULL DEFAULT NULL COMMENT '可用数量',
  `AMOUNT` decimal(12, 2) NULL DEFAULT NULL COMMENT '库存金额',
  `LAST_CHECK` date NULL DEFAULT NULL COMMENT '盘点日期',
  `STOCKTAKING_QUANTITY` int(11) NULL DEFAULT NULL COMMENT '盘点数量',
  `STATE` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `REMARK` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `CREATE_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_CREATE` date NULL DEFAULT NULL,
  `MODIFY_BY` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `GMT_MODIFY` date NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

/*==============================================================*/
/* Table: JXC_SALES_SLIP                                        */
/*==============================================================*/
DROP TABLE IF EXISTS JXC_SALES_SLIP;
CREATE TABLE JXC_SALES_SLIP
(
   ID                   VARCHAR(40) NOT NULL,
   GOODS_ID             VARCHAR(40) COMMENT '商品ID',
   GOODS_NAME           VARCHAR(120) COMMENT '商品名称',
   ITEM_NO              VARCHAR(40) COMMENT '货号',
   ORDER_CODE           VARCHAR(40) COMMENT '订单编号',
   SHIP_PRICE           NUMERIC(14,4) COMMENT '快递价格',
   SHIP_NUMBER          VARCHAR(40) COMMENT '快递单号',
   SHIP_COMPANY         VARCHAR(6) COMMENT '快递公司',
   NUMMER               NUMERIC(14,4) COMMENT '数量',
   PRICE                NUMERIC(14,4) COMMENT '成交价',
   TOTAL_PRICE          NUMERIC(14,4) COMMENT '总成交额',
   COST_PRICE           NUMERIC(14,4) COMMENT '成本价',
   TOTAL_COST           NUMERIC(14,4) COMMENT '总成本',
   PROFIT               NUMERIC(14,4) COMMENT '利润',
   DEAL_TIME            DATE COMMENT '成交时间',
   SOURCE_PLATFORM      VARCHAR(6) COMMENT '来源平台',
   RECEIVER             VARCHAR(40) COMMENT '收货人',
   RECEIVER_ADDR        VARCHAR(200) COMMENT '收货地址',
   RECEIVER_PHONE       VARCHAR(13) COMMENT '收货人手机号',
   SUPPLIER_NAME        VARCHAR(60) COMMENT '供应商',
   SUPPLIER_NO          VARCHAR(40) COMMENT '供应商货号',
   SUPPLIER_PHONE       VARCHAR(60) COMMENT '供应商联系方式',
   REMARK               VARCHAR(300) COMMENT '备注',
   CREATE_BY            VARCHAR(40) COMMENT '创建人',
   GMT_CREATE           DATE COMMENT '创建时间',
   MODIFY_BY            VARCHAR(40) COMMENT '修改人',
   GMT_MODIFY           DATE COMMENT '修改时间',
   PRIMARY KEY (ID)
);


SET FOREIGN_KEY_CHECKS = 1;
