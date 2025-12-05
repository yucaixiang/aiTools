#!/bin/bash

echo "========================================="
echo "   AI工具推荐系统 - 停止脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 停止Docker服务
echo -e "${YELLOW}停止基础服务...${NC}"
cd deployment
docker-compose down
cd ..
echo -e "${GREEN}✓ 所有服务已停止${NC}"
echo ""
echo "========================================="
