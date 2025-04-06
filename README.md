# mcp-server-Sql

## 项目介绍

这是一个基于Spring Boot的SQL服务应用，用于执行SQL查询并将结果写入文件。该服务作为Spring AI MCP（Model, Chat, Prompt）框架的一部分，提供了SQL查询和更新操作的功能。

## 功能特性

- 执行SQL查询并将结果写入CSV文件
- 执行SQL更新操作（INSERT, UPDATE, DELETE）并返回影响的行数
- 支持自定义数据库连接配置
- 集成Spring AI MCP框架，可作为AI工具使用

## 技术栈

- Java 17
- Spring Boot 3.4.3
- Spring AI 1.0.0-M6
- MySQL 8.0
- HikariCP 连接池
- Lombok

## 快速开始

### 配置数据库

在`application.yml`文件中配置您的数据库连接信息：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 构建项目

```bash
mvn clean package
```

### 运行应用

```bash
java -jar target/mcp-server-Sql-1.0-SNAPSHOT.jar
```

## 使用示例

### 执行SQL查询

```json
{
  "sql": "SELECT * FROM users",
  "outputPath": "./output/users.csv"
}
```

### 执行SQL更新操作

```json
{
  "sql": "UPDATE users SET name='John' WHERE id=1",
  "outputPath": "./output/update_result.txt"
}
```

## 注意事项

- 确保输出目录存在或有权限创建
- SQL查询结果将以CSV格式保存
- 更新操作结果将包含影响的行数和执行时间