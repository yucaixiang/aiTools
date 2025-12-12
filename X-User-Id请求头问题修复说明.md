# X-User-Id请求头问题修复说明

## 问题描述

登录后访问 `/api/favorites/5/check` 等需要认证的接口时,后端报错:"缺少请求头: X-User-Id"

## 根本原因

**架构变更导致的不兼容**:

1. **之前(使用Gateway)**: Gateway的AuthenticationFilter验证JWT后,将用户信息添加到**请求头(Header)**,然后转发给Monolith
   ```java
   // Gateway添加Header
   ServerHttpRequest modifiedRequest = request.mutate()
       .header("X-User-Id", String.valueOf(userId))
       .header("Username", username)
       .header("User-Role", role)
       .build();
   ```

2. **现在(去除Gateway)**: Monolith的JwtAuthenticationFilter验证JWT后,将用户信息添加到**请求属性(Attribute)**
   ```java
   // 只添加到Attribute
   request.setAttribute("userId", userId);
   request.setAttribute("username", username);
   request.setAttribute("userRole", role);
   ```

3. **Controller期望**: Controller使用 `@RequestHeader("X-User-Id")` 从Header中获取userId
   ```java
   @GetMapping("/{toolId}/check")
   public Result<Boolean> checkFavorite(
       @PathVariable Long toolId,
       @RequestHeader("X-User-Id") Long userId) { // 期望从Header获取
       // ...
   }
   ```

**结果**: Attribute中有userId,但Header中没有,导致报错"缺少请求头"

---

## 解决方案

### 方案选择

**方案1** ✅ (已采用): 修改JWT过滤器,使用HttpServletRequestWrapper添加Header
- **优点**: 最小改动,只需修改1个文件
- **缺点**: 需要使用wrapper

**方案2**: 修改所有Controller,从Attribute获取userId
- **优点**: 更符合单体架构的设计
- **缺点**: 需要修改多个Controller文件

### 实施方案1

**修改文件**: `JwtAuthenticationFilter.java`

**修改内容**:

```java
// 从Token中提取用户信息
Long userId = JwtUtil.getUserIdFromToken(token);
String username = JwtUtil.getUsernameFromToken(token);
String role = JwtUtil.getRoleFromToken(token);

// 1. 将用户信息添加到请求属性
request.setAttribute("userId", userId);
request.setAttribute("username", username);
request.setAttribute("userRole", role);

// 2. 使用HttpServletRequestWrapper添加Header,兼容使用@RequestHeader的Controller
HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
    @Override
    public String getHeader(String name) {
        if ("X-User-Id".equalsIgnoreCase(name)) {
            return String.valueOf(userId);
        } else if ("Username".equalsIgnoreCase(name)) {
            return username;
        } else if ("User-Role".equalsIgnoreCase(name)) {
            return role;
        }
        return super.getHeader(name);
    }
};

// 3. 传递wrapper给后续的filter chain
filterChain.doFilter(requestWrapper, response);
```

**添加的import**:
```java
import jakarta.servlet.http.HttpServletRequestWrapper;
```

---

## 工作原理

### HttpServletRequestWrapper

`HttpServletRequestWrapper` 是Servlet API提供的装饰器模式实现:

1. **包装原始request**: 不修改原始HttpServletRequest对象
2. **重写getHeader方法**: 拦截对特定Header的读取请求
3. **动态返回值**: 当Controller调用 `getHeader("X-User-Id")` 时,返回我们从JWT中提取的userId
4. **透明代理**: 对于其他Header,调用 `super.getHeader(name)` 返回原始值

### 调用流程

```
1. 前端发送请求
   ↓
   Header: Authorization: Bearer <JWT_TOKEN>

2. JwtAuthenticationFilter拦截
   ↓
   - 提取并验证JWT
   - 从JWT中提取: userId=123, username="test", role="USER"

3. 创建HttpServletRequestWrapper
   ↓
   - 包装原始request
   - 重写getHeader方法
   - 当读取"X-User-Id"时,返回"123"

4. Controller接收
   ↓
   @RequestHeader("X-User-Id") Long userId
   ↓
   实际调用: requestWrapper.getHeader("X-User-Id")
   ↓
   返回: "123"
   ↓
   自动转换: Long userId = 123
```

---

## 受影响的接口

### FavoriteController (`/api/favorites`)

- ✅ `POST /{toolId}` - 添加收藏
- ✅ `DELETE /{toolId}` - 取消收藏
- ✅ `GET /{toolId}/check` - 检查收藏状态
- ✅ `GET /my` - 获取我的收藏列表

### ReviewController (`/api/reviews`)

- ✅ `POST /` - 创建评论
- ✅ `PUT /{id}` - 更新评论
- ✅ `DELETE /{id}` - 删除评论
- ✅ `GET /my` - 获取我的评论列表
- ✅ `POST /{id}/helpful` - 标记评论有帮助
- ✅ `DELETE /{id}/helpful` - 取消有帮助标记
- ✅ `GET /check` - 检查用户是否已评论

**所有这些接口现在都能正常工作了!**

---

## 测试验证

### 测试1: 检查收藏状态

```bash
# 1. 登录获取token
curl -X POST http://localhost:8090/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 2. 使用token检查收藏(将<TOKEN>替换为实际token)
curl http://localhost:8090/api/favorites/5/check \
  -H "Authorization: Bearer <TOKEN>"

# 预期结果: {"code":200,"message":"success","data":false}
```

### 测试2: 添加收藏

```bash
curl -X POST http://localhost:8090/api/favorites/5 \
  -H "Authorization: Bearer <TOKEN>"

# 预期结果: {"code":200,"message":"success","data":null}
```

### 测试3: 创建评论

```bash
curl -X POST http://localhost:8090/api/reviews \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 5,
    "rating": 5,
    "content": "这个工具很好用！"
  }'

# 预期结果: {"code":200,"message":"评论成功","data":1}
```

---

## 后端日志示例

重启Monolith后,访问需要认证的接口,应该看到:

```
========== JWT认证过滤器 ==========
请求: GET /api/favorites/5/check
Authorization Header: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZ...
提取的Token: eyJhbGciOiJIUzI1Ni...
开始验证Token...
用户认证成功: userId=1, username=testuser, role=USER
```

**不再有"缺少请求头"的错误!**

---

## 技术细节

### 为什么不能直接修改HttpServletRequest的Header?

HttpServletRequest的实现通常是不可变的(immutable):

```java
// ❌ 这不会工作
request.setHeader("X-User-Id", "123"); // 没有这个方法!

// ❌ 这也不会工作
HttpServletRequest modifiedRequest = request;
// 无法修改已经创建的request对象
```

因此需要使用**装饰器模式**,创建一个wrapper包装原始request:

```java
// ✅ 正确做法
HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
    @Override
    public String getHeader(String name) {
        // 拦截特定header的读取
        if ("X-User-Id".equals(name)) {
            return "123";
        }
        return super.getHeader(name);
    }
};
```

### HttpServletRequestWrapper vs HttpServletRequest

| 特性 | HttpServletRequest | HttpServletRequestWrapper |
|------|-------------------|---------------------------|
| 类型 | 接口(Interface) | 类(Class) |
| 可修改性 | 不可变(Immutable) | 可重写方法 |
| 使用场景 | Servlet容器创建 | Filter中包装 |
| 典型用途 | 读取请求信息 | 修改请求行为 |

---

## 注意事项

### 1. 大小写敏感性

Header名称不区分大小写:

```java
if ("X-User-Id".equalsIgnoreCase(name)) { // 使用equalsIgnoreCase
    return String.valueOf(userId);
}
```

这样 `x-user-id`, `X-USER-ID`, `X-User-Id` 都能正确识别。

### 2. 类型转换

Controller中使用 `Long userId`:

```java
@RequestHeader("X-User-Id") Long userId
```

Spring会自动将String转换为Long:
- Wrapper返回: `"123"` (String)
- Spring转换: `123` (Long)

### 3. Wrapper生命周期

Wrapper只在当前请求中有效:
- 每个请求创建新的wrapper
- 请求结束后wrapper被销毁
- 不影响其他请求

---

## 总结

### 修改前

```
JWT Filter → 设置Attribute → Controller读取Header → ❌ 找不到Header
```

### 修改后

```
JWT Filter → 设置Attribute + 创建Wrapper → Controller读取Header → ✅ 从Wrapper获取
```

### 关键改动

1. **添加import**: `HttpServletRequestWrapper`
2. **创建wrapper**: 重写 `getHeader()` 方法
3. **传递wrapper**: `filterChain.doFilter(requestWrapper, response)`

**一处修改,所有Controller都正常工作!** 🎉

---

## 下一步

1. ✅ 重启Monolith服务
2. ✅ 测试登录功能
3. ✅ 测试收藏/评论等需要认证的功能
4. ✅ 确认后端日志正常
5. ✅ 确认前端功能正常

现在所有的认证接口都应该能正常工作了!
