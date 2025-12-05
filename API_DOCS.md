# Libms 后端接口文档

说明：本文件列出当前后端已实现的 REST 接口，统一返回格式为：

```json
{ "code": 0, "message": "success", "data": ... }
```

非 0 的 `code` 表示出错；错误统一通过 `ApiResponse.fail(code, message)` 返回，常见业务错误码见各接口说明。

鉴权：

- 使用 JWT Bearer Token（登录 `/login` 获取）。
- 除 `/login`、`/register`、`/actuator/**` 外，其他接口需要认证。
- `GET /books`、`GET /borrows` 等查询接口可被认证用户访问；`/users/**`、`/books/**` 的写操作默认仅限 `ADMIN`（参见 `SecurityConfig`）。

基础响应结构

- 成功示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {...}
}
```

- 业务错误示例：

```json
{
  "code": 4001,
  "message": "该图书存在借阅记录，禁止删除",
  "data": null
}
```

---

## 1. 认证（Auth）

### 1.1 登录

- URL: `POST /login`
- 描述: 用户登录，返回 JWT token
- 请求体 (JSON):

```json
{ "username": "user", "password": "123456" }
```

- 成功响应:

```json
{ "code": 0, "message": "success", "data": { "token": "<jwt>" } }
```

### 1.2 注册

- URL: `POST /register`
- 描述: 注册新用户（仅用于管理/测试）
- 请求体 (JSON):

```json
{
  "username": "alice",
  "password": "123456",
  "fullName": "艾丽丝",
  "role": "USER"
}
```

- 成功响应: `code=0`

注意：注册接口公开，但生产环境请限制或关闭注册。

---

## 2. 用户管理（Users）

说明：用户写操作受角色限制（仅 `ADMIN` 可操作）。返回 DTO 不包含密码字段。

### 2.1 获取当前用户信息

- URL: `GET /users/me`
- 描述: 获取当前登录用户信息（需认证，任意用户均可）
- 鉴权: 认证用户
- 返回 data 示例:

```json
{
  "id": 1,
  "username": "user",
  "fullName": "Regular User",
  "role": "USER"
}
```

### 2.2 获取指定用户

- URL: `GET /users/{id}`
- 描述: 获取指定用户详情
- 鉴权: 需要 `ADMIN` 角色
- 返回 data 示例:

```json
{
  "id": 1,
  "username": "user",
  "fullName": "Regular User",
  "role": "USER"
}
```

### 2.3 分页查询

- URL: `GET /users`
- 描述: 分页与条件筛选
- Query 参数:
  - `username` (可选, 模糊)
  - `role` (可选, 精确)
  - `page` (第几页，1 起)
  - `size` (每页大小)
- 鉴权: 需要 `ADMIN` 角色
- 返回 data 示例:

```json
{
  "total": 2,
  "page": 1,
  "size": 10,
  "list": [ {"id":1,...}, {"id":2,...} ]
}
```

### 2.4 新增用户

- URL: `POST /users`
- 请求体:

```json
{ "username": "bob", "password": "123456", "fullName": "鲍勃", "role": "USER" }
```

- 鉴权: 需要 `ADMIN`

成功返回新用户 ID。

### 2.5 更新用户

- URL: `PUT /users/{id}`
- 请求体:

```json
{ "fullName": "新姓名", "role": "ADMIN" }
```

- 鉴权: 需要 `ADMIN`

### 2.6 修改密码（仅允许本人）

- URL: `PUT /users/password`
- 请求体:

```json
{ "password": "newpass123" }
```

- 鉴权: 仅允许用户本人修改自己的密码，管理员不可修改他人密码。
- 错误码：
  - 403: 只能修改自己的密码

### 2.7 删除用户

- URL: `DELETE /users/{id}`
- 鉴权: 需要 `ADMIN`

---

## 3. 图书管理（Books）

说明：仅管理图书种类与库存，借阅逻辑在 Borrow 模块。写操作受 `ADMIN` 限制。

### 3.1 获取图书

- URL: `GET /books/{id}`
- 描述: 获取图书详情

返回 data 示例:

```json
{
  "id": 1,
  "title": "Java 编程实战",
  "author": "约书亚·布洛赫",
  "isbn": "9780134685991",
  "category": "编程",
  "totalCopies": 5,
  "availableCopies": 5,
  "publishedDate": "2018-01-06"
}
```

### 3.2 分页查询

- URL: `GET /books`
- Query 参数:
  - `title` (模糊)
  - `author` (模糊)
  - `isbn` (精确)
  - `category` (精确)
  - `page`, `size`
- 鉴权: 认证用户（普通用户可访问）

### 3.3 新增图书

- URL: `POST /books`
- 请求体:

```json
{
  "title": "示例",
  "author": "作者",
  "isbn": "978xxxx",
  "category": "编程",
  "totalCopies": 5,
  "availableCopies": 5,
  "publishedDate": "2025-01-01"
}
```

- 业务校验：
  - `availableCopies` 与 `totalCopies` 必须为非负整数，且 `availableCopies <= totalCopies`
  - ISBN 唯一

### 3.4 更新图书

- URL: `PUT /books/{id}`
- 请求体同新增
- 鉴权: `ADMIN`

### 3.5 删除图书

- URL: `DELETE /books/{id}`
- 鉴权: `ADMIN`
- 业务规则：若图书存在借阅记录（借阅表 `borrow_record` 中有任意记录引用该 book_id），删除被禁止，返回业务错误码 `4001`，body:

```json
{ "code": 4001, "message": "该图书存在借阅记录，禁止删除", "data": null }
```

---

## 4. 借阅（Borrows）

说明：借阅与还书会自动调整图书 `available_copies` 字段，且有业务错误码支持。

### 4.1 借书

- URL: `POST /borrows`
- 请求体:

```json
{
  "userId": 1,
  "bookId": 2,
  "borrowDate": "2025-12-03",
  "dueDate": "2025-12-17"
}
```

- 成功返回新借阅记录 ID
- 业务错误码：
  - 4002: 图书不存在
  - 4003: 库存不足，无法借阅

流程：检查图书可用库存 >=1，扣减可用库存并插入 `borrow_record`（status=`BORROWED`）。

### 4.2 还书

- URL: `PUT /borrows/{id}/return`
- 请求体:

```json
{ "returnDate": "2025-12-10" }
```

- 业务错误码：
  - 4004: 借阅记录不存在
  - 4005: 该记录已归还或状态异常

流程：检查记录状态为 `BORROWED`，将图书 `available_copies++`，更新借阅记录 `return_date` 与 `status=RETURNED`。

### 4.3 借阅列表（分页）

- URL: `GET /borrows`
- Query 参数: `userId`、`bookId`、`status`（`BORROWED`/`RETURNED`）、`page`、`size`

返回 data 为分页结果，元素包含借阅记录详情。

### 4.4 根据用户与 ISBN 查询未归还记录

- URL: `GET /borrows/active`
- Query 参数: `userId`（必填）、`isbn`（必填）
- 描述: 返回该用户在指定 ISBN 下所有状态为 `BORROWED` 的借阅记录，用于前端归还列表。
- 返回示例:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 101,
      "userId": 1,
      "bookId": 2,
      "borrowDate": "2025-12-03",
      "dueDate": "2025-12-17",
      "returnDate": null,
      "status": "BORROWED"
    }
  ]
}
```

---

## 5. 错误码总览（常用）

- 0: 成功
- 4001: 该图书存在借阅记录，禁止删除
- 4002: 图书不存在
- 4003: 库存不足，无法借阅
- 4004: 借阅记录不存在
- 4005: 该记录已归还或状态异常
- 401: 未认证或令牌无效
- 403: 权限不足
- 404: 资源不存在（部分接口以 404 返回）
- 500: 系统异常

---

## 6. 安全与测试建议

- 登录后在请求头加入 `Authorization: Bearer <token>`。
- 使用 Apifox/Postman 时把 `Content-Type: application/json` 设置正确；PUT/POST 的入参均以 JSON 为主。
- 管理相关接口（用户/图书写操作）请用管理员账号测试（初始化数据中 `admin`）。

---

若需我把此文档转换为 OpenAPI (Swagger) 规范或生成 Postman/Apifox 导入文件，我可以继续添加对应的输出（yaml/json）。
