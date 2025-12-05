#!/bin/bash

# AI工具推荐系统 - API测试脚本
# 用于测试所有后端API接口的可用性

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# API网关地址
API_BASE="http://localhost:8080"

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Token存储
ACCESS_TOKEN=""
USER_ID=""

# 打印标题
print_header() {
    echo -e "\n${YELLOW}========================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}========================================${NC}\n"
}

# 打印测试结果
print_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC} - $2"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - $2"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 测试API接口
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local auth=$5

    local headers="Content-Type: application/json"
    if [ "$auth" = "true" ] && [ -n "$ACCESS_TOKEN" ]; then
        headers="$headers\nAuthorization: Bearer $ACCESS_TOKEN"
    fi

    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "$headers" "${API_BASE}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "$headers" -d "$data" "${API_BASE}${endpoint}")
    fi

    http_code=$(echo "$response" | tail -n1)

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        print_result 0 "$description"
        echo "$response" | head -n-1
        return 0
    else
        print_result 1 "$description (HTTP $http_code)"
        return 1
    fi
}

# ==================== 开始测试 ====================

print_header "AI工具推荐系统 - API测试"

echo "测试环境: $API_BASE"
echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# ==================== 1. 用户服务测试 ====================

print_header "1. 用户服务测试 (User Service - 8082)"

# 1.1 用户注册
echo "1.1 测试用户注册..."
register_data='{
  "username": "testuser_'$(date +%s)'",
  "email": "test'$(date +%s)'@example.com",
  "password": "Test123456",
  "nickname": "测试用户"
}'

response=$(test_api "POST" "/api/users/register" "$register_data" "用户注册")

# 1.2 用户登录
echo -e "\n1.2 测试用户登录..."
login_data='{
  "account": "testuser",
  "password": "123456"
}'

response=$(curl -s -X POST -H "Content-Type: application/json" \
  -d "$login_data" "${API_BASE}/api/users/login")

http_code=$(echo "$response" | jq -r '.code // 500')

if [ "$http_code" = "200" ]; then
    ACCESS_TOKEN=$(echo "$response" | jq -r '.data.accessToken')
    USER_ID=$(echo "$response" | jq -r '.data.user.id')
    print_result 0 "用户登录 (Token获取成功)"
    echo "Access Token: ${ACCESS_TOKEN:0:50}..."
    echo "User ID: $USER_ID"
else
    print_result 1 "用户登录失败"
fi

# 1.3 获取当前用户信息
echo -e "\n1.3 测试获取用户信息..."
test_api "GET" "/api/users/me" "" "获取当前用户信息" "true"

# 1.4 检查用户名
echo -e "\n1.4 测试检查用户名..."
test_api "GET" "/api/users/check-username?username=testuser" "" "检查用户名是否可用"

# ==================== 2. 工具服务测试 ====================

print_header "2. 工具服务测试 (Tool Service - 8081)"

# 2.1 查询热门工具
echo "2.1 测试查询热门工具..."
test_api "GET" "/api/tools/hot?limit=5" "" "查询热门工具"

# 2.2 查询最新工具
echo -e "\n2.2 测试查询最新工具..."
test_api "GET" "/api/tools/latest?limit=5" "" "查询最新工具"

# 2.3 搜索工具
echo -e "\n2.3 测试搜索工具..."
test_api "GET" "/api/tools/search?keyword=AI&current=1&size=10" "" "搜索工具"

# 2.4 分页查询工具
echo -e "\n2.4 测试分页查询工具..."
query_data='{
  "current": 1,
  "size": 10,
  "categoryId": null,
  "pricingModel": null,
  "minRating": null,
  "sortBy": "hot"
}'
test_api "POST" "/api/tools/query" "$query_data" "分页查询工具"

# 2.5 查询工具详情
echo -e "\n2.5 测试查询工具详情..."
test_api "GET" "/api/tools/1" "" "查询工具详情"

# 2.6 查询分类列表
echo -e "\n2.6 测试查询分类列表..."
test_api "GET" "/api/categories" "" "查询分类列表"

# 2.7 点赞工具 (需要登录)
if [ -n "$ACCESS_TOKEN" ]; then
    echo -e "\n2.7 测试点赞工具..."
    test_api "POST" "/api/tools/1/upvote" "" "点赞工具" "true"
fi

# 2.8 收藏工具 (需要登录)
if [ -n "$ACCESS_TOKEN" ]; then
    echo -e "\n2.8 测试收藏工具..."
    test_api "POST" "/api/tools/1/favorite" "" "收藏工具" "true"
fi

# ==================== 3. AI服务测试 ====================

print_header "3. AI服务测试 (AI Service - 8083)"

# 3.1 AI对话 (需要登录)
if [ -n "$ACCESS_TOKEN" ]; then
    echo "3.1 测试AI对话..."
    chat_data='{
      "message": "我想找一个好用的笔记工具",
      "needRecommendation": true,
      "recommendCount": 3
    }'
    test_api "POST" "/api/ai/chat" "$chat_data" "AI对话" "true"
fi

# 3.2 工具推荐
echo -e "\n3.2 测试工具推荐..."
test_api "GET" "/api/ai/recommend?query=设计工具&limit=5" "" "基于关键词推荐工具"

# 3.3 相似工具推荐
echo -e "\n3.3 测试相似工具推荐..."
test_api "GET" "/api/ai/recommend/similar/1?limit=5" "" "相似工具推荐"

# ==================== 4. 评论服务测试 ====================

print_header "4. 评论服务测试 (Review Service - 8084)"

# 4.1 查询工具评论
echo "4.1 测试查询工具评论..."
test_api "GET" "/api/reviews/tool/1?current=1&size=10" "" "查询工具评论"

# 4.2 查询热门评论
echo -e "\n4.2 测试查询热门评论..."
test_api "GET" "/api/reviews/tool/1/hot?limit=3" "" "查询热门评论"

# 4.3 查询评分分布
echo -e "\n4.3 测试查询评分分布..."
test_api "GET" "/api/reviews/tool/1/rating-distribution" "" "查询评分分布"

# 4.4 创建评论 (需要登录)
if [ -n "$ACCESS_TOKEN" ]; then
    echo -e "\n4.4 测试创建评论..."
    review_data='{
      "toolId": 1,
      "rating": 5,
      "content": "这是一个非常好用的工具，强烈推荐！",
      "pros": ["功能强大", "易于使用"],
      "cons": ["价格略高"]
    }'
    test_api "POST" "/api/reviews" "$review_data" "创建评论" "true"
fi

# ==================== 5. 管理服务测试 ====================

print_header "5. 管理服务测试 (Admin Service - 8085)"

echo "5.1 测试查询统计数据..."
# 注意: 管理接口需要管理员权限，这里仅测试接口可访问性
response=$(curl -s -w "\n%{http_code}" "${API_BASE}/api/admin/tools/statistics" 2>/dev/null || echo -e "\n503")
http_code=$(echo "$response" | tail -n1)

if [ "$http_code" = "200" ] || [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    print_result 0 "管理服务可访问 (HTTP $http_code)"
else
    print_result 1 "管理服务不可访问 (HTTP $http_code)"
fi

# ==================== 测试总结 ====================

print_header "测试总结"

echo "总测试数: $TOTAL_TESTS"
echo -e "通过数量: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败数量: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${RED}✗ 存在失败的测试${NC}"
    exit 1
fi
