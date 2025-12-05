# Libms 图书馆管理系统（后端）

Libms 是一个基于 Spring Boot 的图书馆管理后端，提供用户、图书与借阅的完整接口与权限控制，支持 JWT 登录认证与 OpenAPI 文档展示。

## 特性

- 认证与授权：JWT Bearer Token 登录，基于角色的访问控制
- 业务模块：用户、图书（库存管理）、借阅与还书（自动更新可用库存）
- 统一响应：符合阿里手册风格的统一 JSON 返回结构
- 文档与可视化：内置 OpenAPI/Swagger UI
- 数据初始化：`schema.sql` 与 `data.sql` 便于快速启动

## 技术栈

- Java 17、Spring Boot
- Spring Security（无状态，JWT）、MyBatis
- JJWT、BCrypt
- Springdoc OpenAPI

## 快速开始（使用 Releases JAR）

你已可直接使用 Release 里的可执行 JAR 运行服务：

1. 准备环境

- 安装并配置 Java 17+（`java -version` 确认）
- 可选：准备外部数据库并配置连接（默认使用项目 `application.yml` 的设置）

2. 运行（Windows PowerShell 示例）

```powershell
# 方式 A：默认配置运行
java -jar .\libms-<version>.jar

# 方式 B：指定端口与配置文件
java -jar .\libms-<version>.jar --server.port=8080 --spring.profiles.active=prod
```

3. 访问

- 接口根地址：`http://localhost:8080/`
- OpenAPI 文档（Swagger UI）：`http://localhost:8080/swagger-ui/index.html`

## 配置

应用配置位于 `src/main/resources/application.yml`（JAR 内也包含）。常用覆盖项：

- `server.port`：服务端口
- `spring.datasource.*`：数据库连接（URL、用户名、密码、驱动）
- `jwt.*`：密钥、过期时间等（如已外置配置）

运行时可通过命令行覆盖：

```powershell
java -jar .\libms-<version>.jar --server.port=8081 --spring.datasource.url="jdbc:mysql://localhost:3306/libms"
```

## 数据库（MySQL 8.0）

- 要求：使用 MySQL 8.0 及以上版本。
- 建库建议：创建数据库例如 `libms`，字符集使用 `utf8mb4`、排序规则 `utf8mb4_general_ci`。
- 配置位置：`src/main/resources/application.yml` 中的 `spring.datasource.*`。
  - `spring.datasource.url`: 例如
    `jdbc:mysql://localhost:3306/libms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true`
  - `spring.datasource.username`: 数据库用户名
  - `spring.datasource.password`: 数据库密码
  - `spring.datasource.driver-class-name`: `com.mysql.cj.jdbc.Driver`
- 初始化脚本（库表与样例数据）：
  - 位置：`src/main/resources/db/schema.sql` 和 `src/main/resources/db/data.sql`
  - 默认从 `classpath:db` 加载；如需显式开启或调整位置，可在 `application.yml` 中设置：
    - `spring.sql.init.mode=always`
    - `spring.sql.init.schema-locations=classpath:db/schema.sql`
    - `spring.sql.init.data-locations=classpath:db/data.sql`

## 账号与权限

- 初始化数据通常包含 `admin` 管理员与 `user` 普通用户（详见 `data.sql`）
- 认证：`POST /login` 获取 JWT；其它接口除注册等少数公开端点外均需认证
- 权限：
  - 普通用户可访问查询类接口（如 `GET /books`、`GET /borrows`、`GET /users/me`、`PUT /users/password`）
  - 管理操作（`/users/**`、`/books/**` 的写操作）仅限 `ADMIN`

## 接口文档

- 详尽的接口说明请见仓库根目录的 `API_DOCS.md`
- 在线 Swagger UI 渲染基于 Springdoc，启动后访问 `swagger-ui/index.html`

## 部署建议

- 生产环境使用外部数据库与独立配置文件（`--spring.profiles.active=prod`）
- 使用系统服务或容器编排：
  - Windows 服务：用 NSSM 或 `sc.exe` 注册为服务
  - Docker：可用 `openjdk:17-jdk` 作为基础镜像，将 JAR 复制并暴露端口
- 日志与监控：建议启用 `actuator` 并收集应用日志

### Docker 示例（可选）

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY libms-<version>.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

## 开发与构建（可选）

如需本地构建：

```powershell
# 在项目根目录执行
mvn clean package -DskipTests
# 产物位于 target/libms-<version>.jar
```

## 许可证

本仓库未显式声明开源许可证，默认保留所有权利。如需开源授权，请在 `LICENSE` 中添加相应条款。

## 致谢

- Spring 社区与 Springdoc 项目
- JJWT 与 MyBatis 社区
