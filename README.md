# tilas 智能学习辅助系统

一个用来管理培训机构部门和员工信息的后台管理系统，后端基于 Spring Boot 4 + MyBatis，采用多模块 Maven 结构，包含 JWT 登录认证和 AOP 操作日志。

## 核心业务模块

### 部门管理

对部门的增删改查。

- 查询全部部门、根据 ID 查询单个部门
- 新增部门、修改部门、删除部门

| 请求方式 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/depts` | 查询全部部门 |
| GET | `/depts/{id}` | 根据 ID 查询部门 |
| POST | `/depts` | 新增部门 |
| PUT | `/depts` | 修改部门 |
| DELETE | `/depts?id=1` | 删除部门 |

### 员工管理

员工与工作经历是一对多关系，新增和修改时一起维护。

- 分页查询，支持按姓名模糊匹配、性别、入职日期区间筛选
- 新增员工并保存工作经历
- 修改员工，工作经历采用先删后插的全量覆盖方式
- 批量删除员工，同时删除关联的工作经历
- 查询员工详情，返回包含工作经历列表

| 请求方式 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/emps?page=1&pageSize=10&name=&gender=&begin=&end=` | 分页查询 |
| GET | `/emps/{id}` | 查询员工详情（含工作经历） |
| POST | `/emps` | 新增员工 |
| PUT | `/emps` | 修改员工 |
| DELETE | `/emps?ids=1,2,3` | 批量删除员工 |

### 登录认证

- `POST /login` 接收用户名和密码，校验通过后签发 JWT 令牌
- `TokenInterceptor` 拦截所有业务接口，放行 `/login`
- 从请求头 `token` 中解析令牌，校验失败返回 HTTP 401 和 `{"code":0,"msg":"NOT_LOGIN"}`
- 校验通过后把员工 ID 放入 ThreadLocal，供操作日志记录操作人

### AOP 操作日志

- 自定义注解 `@Log` 标注在增删改方法上
- `OperationLogAspect` 用环绕通知记录操作人 ID、操作时间、类名、方法名、入参、返回值和执行耗时
- 日志写入 `operate_log` 表；写入失败只打警告日志，不影响业务

## 技术栈

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 4.0.8 | 基础框架（Spring Framework 7.0.9） |
| Java | 21 | 开发语言 |
| MyBatis Spring Boot Starter | 4.0.1 | 持久层 |
| PageHelper Spring Boot Starter | 1.4.7 | 分页插件 |
| MySQL | 8.0 | 数据库 |
| mysql-connector-j | 9.7.0 | MySQL 驱动 |
| HikariCP | 7.0.2 | 数据库连接池 |
| JJWT | 0.9.1 | 生成和解析 JWT 令牌 |
| Spring AOP / AspectJ | 1.9.25.1 | 操作日志切面 |
| 阿里云 OSS SDK | 3.17.4 | 头像等文件上传 |
| Jackson | 3.1.5 | JSON 序列化 |
| Tomcat | 11.0.24 | 内嵌 Web 容器 |
| Maven | 3.9+ | 项目构建 |

## 本地启动

### 1. 准备环境

JDK 21、Maven 3.9+、MySQL 8.0。

### 2. 导入数据库

在项目根目录执行，脚本会创建 `tilas` 库、4 张表以及初始测试数据：

```bash
mysql -uroot -p < sql/schema.sql
```

### 3. 修改数据库配置

打开 `tilas-web-management/src/main/resources/application.yml`，把用户名密码改成你本地的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tilas?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 4. 启动项目

```bash
mvn clean package -DskipTests
java -jar tilas-web-management/target/tilas-web-management.jar
```

也可以直接在 IDEA 里运行 `TilasWebManagementApplication`。启动后服务地址是 `http://localhost:8080`。

### 5. 测试账号

| 用户名 | 密码 |
| --- | --- |
| admin | 123456 |
| zhangsan | 123456 |

先调 `POST /login` 拿到 token，再把它放到后续请求的 `token` 请求头里，即可访问业务接口。
