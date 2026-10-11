-- ============================================================================
-- tilas 智能学习辅助系统 —— 数据库初始化脚本
-- ----------------------------------------------------------------------------
-- 适用版本 : MySQL 8.0+
-- 字符集   : utf8mb4 / utf8mb4_0900_ai_ci
-- 幂等性   : 使用 CREATE TABLE IF NOT EXISTS + INSERT IGNORE，
--            可重复执行且不会覆盖已存在的业务数据
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `tilas`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `tilas`;

-- ----------------------------------------------------------------------------
-- 1. 部门表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dept`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
    `name`        varchar(10)  NOT NULL COMMENT '部门名称',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门表';

-- ----------------------------------------------------------------------------
-- 2. 员工表
--    password 默认值 123456，新增员工时由数据库默认值补齐
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `emp`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID,主键',
    `username`    varchar(20)  NOT NULL COMMENT '用户名',
    `password`    varchar(32) DEFAULT '123456' COMMENT '密码',
    `name`        varchar(10)  NOT NULL COMMENT '姓名',
    `gender`      tinyint unsigned NOT NULL COMMENT '性别, 1:男, 2:女',
    `phone`       char(11)     NOT NULL COMMENT '手机号',
    `job`         tinyint unsigned DEFAULT NULL COMMENT '职位, 1 班主任, 2 讲师, 3 学工主管, 4 教研主管, 5 咨询师',
    `salary`      int unsigned DEFAULT NULL COMMENT '薪资',
    `image`       varchar(255) DEFAULT NULL COMMENT '头像',
    `entry_date`  date         DEFAULT NULL COMMENT '入职日期',
    `dept_id`     int unsigned DEFAULT NULL COMMENT '部门ID',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`),
    UNIQUE KEY `phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='员工表';

-- ----------------------------------------------------------------------------
-- 3. 工作经历表
--    注意：begin / end 为 MySQL 关键字，SQL 中必须使用反引号包裹
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `emp_expr`
(
    `id`      int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
    `emp_id`  int unsigned DEFAULT NULL COMMENT '员工ID',
    `begin`   date         DEFAULT NULL COMMENT '开始时间',
    `end`     date         DEFAULT NULL COMMENT '结束时间',
    `company` varchar(50)  DEFAULT NULL COMMENT '公司名称',
    `job`     varchar(50)  DEFAULT NULL COMMENT '职位',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='工作经历';

-- ----------------------------------------------------------------------------
-- 4. 操作日志表
--    method_params / return_value 长度与切面截断策略保持一致
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `operate_log`
(
    `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `operate_emp_id` int unsigned DEFAULT NULL COMMENT '操作人ID',
    `operate_time`   datetime     DEFAULT NULL COMMENT '操作时间',
    `class_name`     varchar(100) DEFAULT NULL COMMENT '操作的类名',
    `method_name`    varchar(100) DEFAULT NULL COMMENT '操作的方法名',
    `method_params`  varchar(1000) DEFAULT NULL COMMENT '方法参数',
    `return_value`   varchar(2000) DEFAULT NULL COMMENT '返回值, 存储json格式',
    `cost_time`      int          DEFAULT NULL COMMENT '方法执行耗时, 单位:ms',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='操作日志表';

-- ============================================================================
-- 初始化测试数据
-- ============================================================================

-- 部门
INSERT IGNORE INTO `dept` (`id`, `name`, `create_time`, `update_time`)
VALUES (1, '学工部', NOW(), NOW()),
       (2, '教研部', NOW(), NOW()),
       (3, '咨询部', NOW(), NOW()),
       (4, '就业部', NOW(), NOW()),
       (5, '人事部', NOW(), NOW());

-- 员工
-- 测试账号：admin / 123456（教研主管）
INSERT IGNORE INTO `emp` (`id`, `username`, `password`, `name`, `gender`, `phone`, `job`, `salary`,
                          `image`, `entry_date`, `dept_id`, `create_time`, `update_time`)
VALUES (1, 'admin', '123456', '管理员', 1, '13800000001', 4, 20000, NULL, '2020-03-01', 2, NOW(), NOW()),
       (2, 'zhangsan', '123456', '张三', 1, '13800000002', 2, 15000, NULL, '2021-06-15', 2, NOW(), NOW()),
       (3, 'lisi', '123456', '李四', 2, '13800000003', 2, 14000, NULL, '2021-09-01', 2, NOW(), NOW()),
       (4, 'wangwu', '123456', '王五', 1, '13800000004', 3, 13000, NULL, '2022-02-20', 1, NOW(), NOW()),
       (5, 'zhaoliu', '123456', '赵六', 2, '13800000005', 5, 12000, NULL, '2022-07-01', 3, NOW(), NOW()),
       (6, 'sunqi', '123456', '孙七', 1, '13800000006', 5, 11000, NULL, '2023-03-10', 3, NOW(), NOW()),
       (7, 'zhouba', '123456', '周八', 2, '13800000007', 1, 10000, NULL, '2023-08-01', 1, NOW(), NOW()),
       (8, 'wujiu', '123456', '吴九', 1, '13800000008', 4, 18000, NULL, '2024-01-05', 4, NOW(), NOW());

-- 工作经历
INSERT IGNORE INTO `emp_expr` (`id`, `emp_id`, `begin`, `end`, `company`, `job`)
VALUES (1, 2, '2017-07-01', '2019-06-30', '杭州云栖科技有限公司', 'Java 开发工程师'),
       (2, 2, '2019-07-01', '2021-05-31', '上海数智信息技术有限公司', '高级 Java 开发工程师'),
       (3, 3, '2018-09-01', '2021-08-31', '北京星辰网络科技有限公司', 'Java 开发工程师'),
       (4, 4, '2019-03-01', '2022-01-31', '深圳前海数据服务有限公司', '技术支持工程师'),
       (5, 5, '2020-07-01', '2022-06-30', '广州智联教育科技有限公司', '课程咨询师'),
       (6, 6, '2021-08-01', '2023-02-28', '成都未来人才服务有限公司', '咨询顾问');
