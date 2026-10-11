# Enterprise-Org-Management

企业级一体化组织架构与综合运营管控中台。

面向多事业部、多组织单元的企业内部管理场景，统一收口组织主数据与人员档案的维护入口，并为所有敏感变更操作提供可回溯的审计链路。后端基于 Spring Boot 与 MyBatis 构建，采用 Maven 多模块分层设计，对外提供标准化 RESTful 接口。

## 一、业务定位

平台定位为企业内部的**组织与人员管理中台**，重点解决三个问题：

1. **组织主数据的一致性**。以部门（组织单元）为最小管理粒度，统一维护组织主数据及其变更时间戳，避免各业务系统各自维护造成的数据分叉。
2. **人员档案的完整归档**。员工基础信息与组织归属、岗位、薪资、入职信息、历史工作经历绑定管理，支持多条件组合检索与批量数据流转。
3. **敏感操作的合规可追溯**。所有增删改操作全链路留痕，可回溯到操作人、操作时间、目标类与方法、入参、返回值与执行耗时。

## 二、技术栈

以下版本与 `pom.xml` 及各模块依赖声明保持一致。

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Java | 21 | 开发语言 |
| Spring Boot | 4.0.8 | 基础框架（Spring Framework 7.0.9） |
| Spring MVC | 随 Boot 版本 | Web 层，RESTful 接口 |
| MyBatis Spring Boot Starter | 4.0.1 | 持久层框架（MyBatis 3.5.19） |
| PageHelper Spring Boot Starter | 1.4.7 | 物理分页插件（核心 5.3.3） |
| MySQL | 8.0 | 关系型数据库 |
| mysql-connector-j | 9.7.0 | MySQL 驱动 |
| HikariCP | 7.0.2 | 数据库连接池 |
| JJWT | 0.9.1 | JWT 令牌签发与解析（HS256） |
| Spring AOP / AspectJ | 1.9.25.1 | 操作日志切面 |
| Jackson | 3.1.5 | JSON 序列化 |
| 阿里云 OSS SDK | 3.17.4 | 附件上传 |
| 内嵌 Tomcat | 11.0.24 | Web 容器 |
| Maven | 3.9+ | 多模块构建 |

## 三、核心业务模块

### 1. 组织单元与部门主数据维护

- 组织单元完整生命周期管理：全量查询、按 ID 查询、新增、修改、删除。
- 部门名称由数据库唯一索引约束，重复提交时由全局异常处理器转换为可读的业务提示，不向前端暴露异常堆栈。
- 新增与修改统一维护 `create_time` / `update_time`，为组织变更追溯提供时间依据。

### 2. 人员信息多条件组合分页检索与批量数据流转

- 分页检索支持姓名模糊匹配、性别精确匹配、入职日期区间三类条件的任意组合，条件为空时由 MyBatis 动态 SQL 自动裁剪，避免拼接无效子句。
- 列表查询显式声明返回字段，不返回 `password` 等敏感列。
- 员工与工作经历为一对多关系：新增时批量写入工作经历，修改时采用「先删后插」全量覆盖，删除时按员工 ID 批量级联清理。
- 上述多表操作统一由 `@Transactional(rollbackFor = Exception.class)` 保证原子性，任一步骤失败整体回滚。
- 员工详情通过自定义 `resultMap` 与 `collection` 映射，一次性把左连接结果装配为工作经历列表，规避 N+1 查询。

### 3. 基于 JWT 的统一访问鉴权与全局登录态校验

- 登录接口校验凭证后签发 JWT，负载携带员工 ID 与用户名；签名密钥与有效期通过 `application.yml` 占位符外置，不硬编码。
- `TokenInterceptor` 拦截全量业务路径，放行登录接口、错误页、静态资源与 CORS 预检请求。
- 令牌缺失、签名非法或已过期时，统一返回 HTTP 401 与约定响应体 `{"code":0,"msg":"NOT_LOGIN"}`，前端据此重定向登录页。
- 服务端不保存会话状态，任意实例均可独立完成鉴权，便于水平扩容。

### 4. 基于 Spring AOP 的敏感操作全链路审计留痕

- 自定义注解 `@Log` 标注于 Controller 的增删改方法，`OperationLogAspect` 以环绕通知统一拦截。
- 记录维度包括：操作人 ID、操作时间、目标类名与方法名、入参 JSON、返回值 JSON、方法执行耗时。
- 日志写入置于 `finally` 块，目标方法抛出异常时同样留痕，并在返回值字段标注异常信息。
- 入参与返回值按 `operate_log` 表字段长度截断后入库；日志写入失败仅降级为告警日志，不影响主业务流程。

### 5. 基于 ControllerAdvice 的全局异常拦截与标准化响应封装

- `GlobalExceptionHandler` 以 `@RestControllerAdvice` 统一收口异常，区分唯一约束冲突与未知异常，分别返回可读提示与兜底文案。
- 所有接口返回统一包装类 `Result{code, msg, data}`，`code = 1` 表示成功、`code = 0` 表示失败，前端只需一套解析逻辑。
- 分页结果统一封装为 `PageResult{total, rows}`。

## 四、工程结构

```
Enterprise-Org-Management
├── pom.xml                     父工程，统一依赖版本与构建配置
├── sql/schema.sql              数据库初始化脚本
├── tilas-pojo/                 领域模型与传输对象（实体、查询参数、统一响应体）
├── tilas-utils/                通用能力组件（JWT 工具、登录上下文持有器、OSS 操作器）
└── tilas-web-management/       应用层（Controller / Service / Mapper / Interceptor / AOP / 异常处理）
```

## 五、接口概览

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/login` | 登录并签发 JWT | 免登录 |
| GET | `/depts` | 查询全部组织单元 | 需 token |
| GET | `/depts/{id}` | 按 ID 查询组织单元 | 需 token |
| POST | `/depts` | 新增组织单元 | 需 token |
| PUT | `/depts` | 修改组织单元 | 需 token |
| DELETE | `/depts?id=1` | 删除组织单元 | 需 token |
| GET | `/emps?page=1&pageSize=10&name=&gender=&begin=&end=` | 分页多条件检索人员 | 需 token |
| GET | `/emps/{id}` | 人员详情（含工作经历） | 需 token |
| POST | `/emps` | 新增人员 | 需 token |
| PUT | `/emps` | 修改人员 | 需 token |
| DELETE | `/emps?ids=1,2,3` | 批量删除人员 | 需 token |
| POST | `/upload` | 附件上传至 OSS | 需 token |
| GET | `/report/empJobData` | 岗位人数分布统计 | 需 token |
| GET | `/report/empGenderData` | 性别分布统计 | 需 token |

## 六、本地启动

### 1. 环境准备

JDK 21、Maven 3.9+、MySQL 8.0。

### 2. 导入数据库

在工程根目录执行。脚本会创建 `tilas` 库、4 张表（`dept`、`emp`、`emp_expr`、`operate_log`）以及初始数据，可重复执行：

```bash
mysql -uroot -p < sql/schema.sql
```

### 3. 检查连接参数

打开 `tilas-web-management/src/main/resources/application.yml`，确认数据源配置与本地环境一致。所有敏感项均支持环境变量覆盖，未设置时回退到本地默认值：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tilas?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

JWT 签名密钥与有效期位于同一文件的 `tlias.jwt` 节点下。

### 4. 启动应用

在 IDE 中直接运行 `com.itheima.TilasWebManagementApplication`，或使用命令行打包后启动：

```bash
mvn clean package -DskipTests
java -jar tilas-web-management/target/tilas-web-management.jar
```

服务默认监听 `http://localhost:8080`。

### 5. 验证

先用测试账号调用 `POST /login` 获取令牌，再将其放入后续请求的 `token` 请求头。

| 用户名 | 密码 |
| --- | --- |
| admin | 123456 |
| zhangsan | 123456 |

## 七、License

本项目基于 [MIT License](LICENSE) 开源。
