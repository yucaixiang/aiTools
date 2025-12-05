#!/bin/bash

echo "========================================="
echo "   AI工具推荐系统 - 一键启动脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查Docker是否运行
echo -e "${YELLOW}[1/5] 检查Docker环境...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker未运行，请先启动Docker${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker环境正常${NC}"
echo ""

# 启动基础服务
echo -e "${YELLOW}[2/5] 启动基础服务（MySQL、Redis、Elasticsearch等）...${NC}"
cd deployment
docker-compose up -d

# 等待服务启动
echo -e "${YELLOW}等待服务启动（30秒）...${NC}"
sleep 30
echo -e "${GREEN}✓ 基础服务启动完成${NC}"
echo ""

# 初始化数据库
echo -e "${YELLOW}[3/5] 初始化数据库...${NC}"
cd ..
echo -e "${YELLOW}执行数据库结构初始化...${NC}"
docker exec -i tool-recommend-mysql mysql -uroot -proot123456 < scripts/init-db.sql
echo -e "${YELLOW}导入示例数据...${NC}"
docker exec -i tool-recommend-mysql mysql -uroot -proot123456 tool_recommend < scripts/init-sample-data.sql
echo -e "${GREEN}✓ 数据库初始化完成${NC}"
echo ""

# 编译后端项目
echo -e "${YELLOW}[4/5] 编译后端项目...${NC}"
cd backend
if command -v mvn &> /dev/null; then
    echo -e "${YELLOW}使用Maven编译项目...${NC}"
    mvn clean install -DskipTests
    echo -e "${GREEN}✓ 后端项目编译完成${NC}"
else
    echo -e "${YELLOW}未检测到Maven，跳过编译${NC}"
fi
cd ..
echo ""

# 启动后端服务
echo -e "${YELLOW}[5/5] 启动后端服务...${NC}"
echo -e "${YELLOW}提示：您可以使用IDEA打开backend目录，手动启动各个服务${NC}"
echo ""

# 显示服务地址
echo "========================================="
echo -e "${GREEN}   所有服务已启动！${NC}"
echo "========================================="
echo ""
echo "📦 基础服务："
echo "  - MySQL:          localhost:3306 (root/root123456)"
echo "  - Redis:          localhost:6379 (密码: redis123456)"
echo "  - Elasticsearch:  http://localhost:9200"
echo "  - Kibana:         http://localhost:5601"
echo "  - Qdrant:         http://localhost:6333"
echo "  - Ollama:         http://localhost:11434"
echo "  - Portainer:      https://localhost:9443"
echo ""
echo "🚀 后端服务（需要手动启动）："
echo "  - Tool Service:   http://localhost:8081"
echo "  - User Service:   http://localhost:8082"
echo ""
echo "📖 使用说明："
echo "  1. 使用IDEA打开 backend 目录"
echo "  2. 启动 ToolServiceApplication"
echo "  3. 启动 UserServiceApplication"
echo "  4. 访问 http://localhost:8081/api/tools/hot 测试"
echo ""
echo "🛑 停止所有服务："
echo "  ./stop-all.sh"
echo ""
echo "========================================="
