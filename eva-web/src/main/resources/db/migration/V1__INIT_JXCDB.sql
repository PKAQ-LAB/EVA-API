DROP TABLE IF EXISTS JXC_BASE_CATEGORY;

DROP TABLE IF EXISTS JXC_BASE_GOODS;

DROP TABLE IF EXISTS JXC_BASE_SUPPLIIERS;

DROP TABLE IF EXISTS JXC_CASHJOURNAL;

DROP TABLE IF EXISTS JXC_PORDER_LINE;

DROP TABLE IF EXISTS JXC_PURCHASING_ORDER;

DROP TABLE IF EXISTS JXC_RETAILSALE;

DROP TABLE IF EXISTS JXC_STOCK;

/*==============================================================*/
/* Table: JXC_BASE_CATEGORY                                     */
/*==============================================================*/
CREATE TABLE JXC_BASE_CATEGORY
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   CODE                 VARCHAR(40) COMMENT '分类编码',
   NAME                 VARCHAR(40) COMMENT '分类名称',
   PARENT_ID            VARCHAR(40) COMMENT '父节点ID',
   PARENT_NAME          VARCHAR(40) COMMENT '父节点名称',
   PATH                 VARCHAR(1200) COMMENT '路径',
   PATH_ID              VARCHAR(1200) COMMENT '路径ID',
   PATH_NAME            VARCHAR(1200) COMMENT '路径描述',
   ISLEAF               TINYINT COMMENT '是否叶子',
   ORDERS               INT COMMENT '排序',
   STATUS               VARCHAR(8) COMMENT '状态',
   REMARK               VARCHAR(400) COMMENT '备注',
   GMT_CREATE           DATETIME COMMENT '创建时间',
   GMT_MODIFY           DATETIME COMMENT '修改时间',
   CREATE_BY            VARCHAR(16) COMMENT '创建人',
   MODIFY_BY            VARCHAR(16) COMMENT '修改人',
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: JXC_BASE_GOODS                                        */
/*==============================================================*/
CREATE TABLE JXC_BASE_GOODS
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   NAME                 VARCHAR(40) COMMENT '品名',
   CATEGORY             VARCHAR(12) COMMENT '品类',
   MODEL                VARCHAR(40) COMMENT '产品型号',
   BARCODE              VARCHAR(60) COMMENT '条码',
   UNIT                 VARCHAR(4) COMMENT '规格单位',
   MNEMONIC             VARCHAR(12) COMMENT '助记码（拼音）',
   BOXUNIT              INT COMMENT '装箱规格',
   REMARK               VARCHAR(300) COMMENT '备注',
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: JXC_BASE_SUPPLIIERS                                   */
/*==============================================================*/
CREATE TABLE JXC_BASE_SUPPLIIERS
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   FULL_NAME            VARCHAR(120) COMMENT '全称',
   NAME                 VARCHAR(30) COMMENT '简称',
   MNEMONIC             VARCHAR(12) COMMENT '助记码',
   CATEGORY             VARCHAR(6) COMMENT '类型(0001 生产厂家，0002 代理商)',
   LINKMAN              VARCHAR(16) COMMENT '联系人',
   MOBILE               VARCHAR(16) COMMENT '联系方式',
   ADDRESS              VARCHAR(200) COMMENT '地址',
   STATUS               VARCHAR(8) COMMENT '状态',
   REMARK               VARCHAR(400) COMMENT '备注',
   GMT_CREATE           DATETIME COMMENT '创建时间',
   GMT_MODIFY           DATETIME COMMENT '修改时间',
   CREATE_BY            VARCHAR(16) COMMENT '创建人',
   MODIFY_BY            VARCHAR(16) COMMENT '修改人',
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: JXC_CASHJOURNAL                                       */
/*==============================================================*/
CREATE TABLE JXC_CASHJOURNAL
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   YEAR                 VARCHAR(4),
   MONTH                VARCHAR(4),
   DAY                  VARCHAR(4),
   CATEGORY             VARCHAR(4),
   NUMS                 INT,
   PRICE                NUMERIC(12,4),
   TOTAL                NUMERIC(12,4),
   REMARK               VARCHAR(300),
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: JXC_PORDER_LINE                                       */
/*==============================================================*/
CREATE TABLE JXC_PORDER_LINE
(
   LINE_ID              VARCHAR(40) NOT NULL COMMENT 'LINE_ID',
   MAIN_ID              VARCHAR(40),
   GOODS_ID             VARCHAR(60) COMMENT '商品ID',
   NAME                 VARCHAR(40) COMMENT '品名',
   CATEGORY             VARCHAR(12) COMMENT '品类',
   MODEL                VARCHAR(40) COMMENT '产品型号',
   BARCODE              VARCHAR(60) COMMENT '条码',
   UNIT                 VARCHAR(4) COMMENT '规格单位',
   MNEMONIC             VARCHAR(12) COMMENT '助记码（拼音）',
   NUM                  NUMERIC(12,4) COMMENT '进货数量',
   VAT                  NUMERIC(12,4) COMMENT '含税单价',
   VAT_AMOUNT           NUMERIC(12,4) COMMENT '含税金额',
   TAX_RATE             NUMERIC(12,4) COMMENT '税率',
   TAX_AMOUNT           NUMERIC(12,4) COMMENT '税额',
   NOTAX_VALUE          NUMERIC(12,4) COMMENT '不含税单价',
   NOTAX_AMOUNT         NUMERIC(12,4) COMMENT '不含税金额',
   STOCK_NUM            NUMERIC(12,4) COMMENT '库存数量',
   STOCK_AMOUNT         NUMERIC(12,4) COMMENT '库存金额',
   PRIMARY KEY (LINE_ID)
);

/*==============================================================*/
/* Table: JXC_PURCHASING_ORDER                                  */
/*==============================================================*/
CREATE TABLE JXC_PURCHASING_ORDER
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   CODE                 VARCHAR(60) COMMENT '入库单号',
   ORDER_DATE           DATE COMMENT '入库日期',
   STOCK                VARCHAR(40) COMMENT '仓库',
   PURCHASING_TYPE      VARCHAR(12) COMMENT '采购类型',
   OPERATOR             VARCHAR(40) COMMENT '制单人',
   OPERATOR_NM          VARCHAR(40) COMMENT '制单人名称',
   PURCHASER            VARCHAR(40) COMMENT '采购人',
   PURCHASER_NM         VARCHAR(40) COMMENT '采购人名称',
   SUPPLIER_ID          VARCHAR(40) COMMENT '供应商',
   SUPPLIER_NM          VARCHAR(80) COMMENT '供应商名称',
   REMARK               VARCHAR(300) COMMENT '备注',
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

ALTER TABLE JXC_PURCHASING_ORDER COMMENT '采购入库单';

/*==============================================================*/
/* Table: JXC_RETAILSALE                                        */
/*==============================================================*/
CREATE TABLE JXC_RETAILSALE
(
   ID                   VARCHAR(40) NOT NULL,
   GOODS_ID             VARCHAR(40),
   ORDER_CODE           VARCHAR(40),
   NUMMER               NUMERIC(14,4),
   PRICE                NUMERIC(14,4),
   TOTAL                NUMERIC(14,4),
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: JXC_STOCK                                             */
/*==============================================================*/
CREATE TABLE JXC_STOCK
(
   ID                   VARCHAR(40) NOT NULL COMMENT 'ID',
   GOODS_ID             VARCHAR(40) COMMENT '商品ID',
   MNEMONIC             VARCHAR(12) COMMENT '助记码（拼音）',
   BARCODE              VARCHAR(60) COMMENT '条码',
   MODEL                VARCHAR(40) COMMENT '产品型号',
   NAME                 VARCHAR(40) COMMENT '品名',
   CATEGORY             VARCHAR(4) COMMENT '品类',
   UNIT_PRICE           NUMERIC(12,2) COMMENT '单价',
   UNIT                 VARCHAR(4) COMMENT '规格单位',
   STORAGE_ID           VARCHAR(40) COMMENT '存放仓库',
   ALLOCATION           VARCHAR(60) COMMENT '货位',
   ONHAND               INT COMMENT '库存数量',
   AVAILABLE            INT COMMENT '可用数量',
   AMOUNT               NUMERIC(12,2) COMMENT '库存金额',
   LAST_CHECK           DATE COMMENT '盘点日期',
   STOCKTAKING_QUANTITY INT COMMENT '盘点数量',
   STATE                VARCHAR(4),
   REMARK               VARCHAR(300) COMMENT '备注',
   CREATE_BY            VARCHAR(40),
   GMT_CREATE           DATE,
   MODIFY_BY            VARCHAR(40),
   GMT_MODIFY           DATE,
   PRIMARY KEY (ID)
);

ALTER TABLE JXC_PORDER_LINE ADD CONSTRAINT FK_FK_INSTOCK FOREIGN KEY (MAIN_ID)
      REFERENCES JXC_PURCHASING_ORDER (ID) ON DELETE RESTRICT ON UPDATE RESTRICT;
