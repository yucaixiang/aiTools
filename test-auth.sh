#!/bin/bash

echo "=========================================="
echo "测试认证功能"
echo "=========================================="
echo ""

# 首先注册一个测试用户
echo "1. 注册测试用户..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8090/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test123",
    "email": "test123@test.com",
    "password": "123456"
  }')

echo "注册响应: $REGISTER_RESPONSE"
echo ""

# 登录获取token
echo "2. 登录获取Token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8090/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "test123",
    "password": "123456"
  }')

echo "登录响应: $LOGIN_RESPONSE"
echo ""

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('data', {}).get('token', ''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "❌ 无法获取Token，登录失败"
  exit 1
fi

echo "✅ Token获取成功: ${TOKEN:0:50}..."
echo ""

# 测试需要认证的接口
echo "3. 测试收藏检查接口 (需要认证)..."
FAVORITE_RESPONSE=$(curl -s http://localhost:8090/api/favorites/1/check \
  -H "Authorization: Bearer $TOKEN")

echo "收藏检查响应: $FAVORITE_RESPONSE"
echo ""

# 检查是否还有"未授权"错误
if echo "$FAVORITE_RESPONSE" | grep -q "未授权"; then
  echo "❌ 测试失败: 仍然提示未授权"
  echo ""
  echo "请检查:"
  echo "1. IntelliJ IDEA中是否已重新编译项目 (Build -> Rebuild Project)"
  echo "2. 是否已重启Monolith服务"
  echo "3. 查看后端日志中是否有JWT认证成功的消息"
  exit 1
fi

if echo "$FAVORITE_RESPONSE" | grep -q "缺少请求头"; then
  echo "❌ 测试失败: 仍然提示缺少请求头 X-User-Id"
  echo ""
  echo "这说明HttpServletRequestWrapper修复还没有生效"
  echo "请确保已重新编译并重启服务"
  exit 1
fi

echo "✅ 测试成功: 认证通过，没有未授权错误"
echo ""
echo "=========================================="
echo "所有测试通过! 🎉"
echo "=========================================="
