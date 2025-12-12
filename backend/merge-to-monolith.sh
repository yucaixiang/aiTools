#!/bin/bash

# 微服务合并为单体应用脚本
# 日期: 2025-12-10
# 作者: AI Tool Recommend Team

set -e  # 遇到错误立即退出

echo "========================================="
echo "微服务合并为单体应用"
echo "========================================="

# 定义颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 工作目录
BACKEND_DIR="/Users/bjsttlp324/Desktop/tools/backend"
MONOLITH_DIR="$BACKEND_DIR/tool-recommend-monolith"

echo -e "${YELLOW}步骤1: 创建单体项目目录结构${NC}"
mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend"
mkdir -p "$MONOLITH_DIR/src/main/resources/mapper/user"
mkdir -p "$MONOLITH_DIR/src/main/resources/mapper/tool"
mkdir -p "$MONOLITH_DIR/src/main/resources/mapper/review"
mkdir -p "$MONOLITH_DIR/src/main/resources/mapper/admin"
mkdir -p "$MONOLITH_DIR/src/test/java/com/toolrecommend"

# 创建模块目录
for module in common user tool review ai admin config; do
    mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/$module"
done

echo -e "${GREEN}✓ 目录结构创建完成${NC}"

echo -e "${YELLOW}步骤2: 复制公共模块${NC}"
if [ -d "$BACKEND_DIR/common/src/main/java/com/toolrecommend/common" ]; then
    cp -r "$BACKEND_DIR/common/src/main/java/com/toolrecommend/common"/* \
          "$MONOLITH_DIR/src/main/java/com/toolrecommend/common/" 2>/dev/null || true
    echo -e "${GREEN}✓ 公共模块复制完成${NC}"
else
    echo -e "${RED}✗ 公共模块不存在${NC}"
fi

echo -e "${YELLOW}步骤3: 复制用户模块${NC}"
if [ -d "$BACKEND_DIR/user-service/src/main/java/com/toolrecommend/user" ]; then
    # 复制Java代码
    for subdir in controller service mapper exception config; do
        if [ -d "$BACKEND_DIR/user-service/src/main/java/com/toolrecommend/user/$subdir" ]; then
            mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/user/$subdir"
            cp -r "$BACKEND_DIR/user-service/src/main/java/com/toolrecommend/user/$subdir"/* \
                  "$MONOLITH_DIR/src/main/java/com/toolrecommend/user/$subdir/" 2>/dev/null || true
        fi
    done

    # 复制Mapper XML
    if [ -d "$BACKEND_DIR/user-service/src/main/resources/mapper" ]; then
        cp -r "$BACKEND_DIR/user-service/src/main/resources/mapper"/* \
              "$MONOLITH_DIR/src/main/resources/mapper/user/" 2>/dev/null || true
    fi

    echo -e "${GREEN}✓ 用户模块复制完成${NC}"
else
    echo -e "${RED}✗ 用户服务不存在${NC}"
fi

echo -e "${YELLOW}步骤4: 复制工具模块${NC}"
if [ -d "$BACKEND_DIR/tool-service/src/main/java/com/toolrecommend/tool" ]; then
    for subdir in controller service mapper exception config; do
        if [ -d "$BACKEND_DIR/tool-service/src/main/java/com/toolrecommend/tool/$subdir" ]; then
            mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/tool/$subdir"
            cp -r "$BACKEND_DIR/tool-service/src/main/java/com/toolrecommend/tool/$subdir"/* \
                  "$MONOLITH_DIR/src/main/java/com/toolrecommend/tool/$subdir/" 2>/dev/null || true
        fi
    done

    if [ -d "$BACKEND_DIR/tool-service/src/main/resources/mapper" ]; then
        cp -r "$BACKEND_DIR/tool-service/src/main/resources/mapper"/* \
              "$MONOLITH_DIR/src/main/resources/mapper/tool/" 2>/dev/null || true
    fi

    echo -e "${GREEN}✓ 工具模块复制完成${NC}"
else
    echo -e "${RED}✗ 工具服务不存在${NC}"
fi

echo -e "${YELLOW}步骤5: 复制评论模块${NC}"
if [ -d "$BACKEND_DIR/review-service/src/main/java/com/toolrecommend/review" ]; then
    for subdir in controller service mapper exception config; do
        if [ -d "$BACKEND_DIR/review-service/src/main/java/com/toolrecommend/review/$subdir" ]; then
            mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/review/$subdir"
            cp -r "$BACKEND_DIR/review-service/src/main/java/com/toolrecommend/review/$subdir"/* \
                  "$MONOLITH_DIR/src/main/java/com/toolrecommend/review/$subdir/" 2>/dev/null || true
        fi
    done

    if [ -d "$BACKEND_DIR/review-service/src/main/resources/mapper" ]; then
        cp -r "$BACKEND_DIR/review-service/src/main/resources/mapper"/* \
              "$MONOLITH_DIR/src/main/resources/mapper/review/" 2>/dev/null || true
    fi

    echo -e "${GREEN}✓ 评论模块复制完成${NC}"
else
    echo -e "${RED}✗ 评论服务不存在${NC}"
fi

echo -e "${YELLOW}步骤6: 复制AI模块${NC}"
if [ -d "$BACKEND_DIR/ai-service/src/main/java/com/toolrecommend/ai" ]; then
    for subdir in controller service exception config; do
        if [ -d "$BACKEND_DIR/ai-service/src/main/java/com/toolrecommend/ai/$subdir" ]; then
            mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/ai/$subdir"
            cp -r "$BACKEND_DIR/ai-service/src/main/java/com/toolrecommend/ai/$subdir"/* \
                  "$MONOLITH_DIR/src/main/java/com/toolrecommend/ai/$subdir/" 2>/dev/null || true
        fi
    done

    echo -e "${GREEN}✓ AI模块复制完成${NC}"
else
    echo -e "${YELLOW}⚠ AI服务不存在,跳过${NC}"
fi

echo -e "${YELLOW}步骤7: 复制管理模块${NC}"
if [ -d "$BACKEND_DIR/admin-service/src/main/java/com/toolrecommend/admin" ]; then
    for subdir in controller service mapper exception config; do
        if [ -d "$BACKEND_DIR/admin-service/src/main/java/com/toolrecommend/admin/$subdir" ]; then
            mkdir -p "$MONOLITH_DIR/src/main/java/com/toolrecommend/admin/$subdir"
            cp -r "$BACKEND_DIR/admin-service/src/main/java/com/toolrecommend/admin/$subdir"/* \
                  "$MONOLITH_DIR/src/main/java/com/toolrecommend/admin/$subdir/" 2>/dev/null || true
        fi
    done

    if [ -d "$BACKEND_DIR/admin-service/src/main/resources/mapper" ]; then
        cp -r "$BACKEND_DIR/admin-service/src/main/resources/mapper"/* \
              "$MONOLITH_DIR/src/main/resources/mapper/admin/" 2>/dev/null || true
    fi

    echo -e "${GREEN}✓ 管理模块复制完成${NC}"
else
    echo -e "${YELLOW}⚠ 管理服务不存在,跳过${NC}"
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}代码复制完成!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${YELLOW}下一步操作:${NC}"
echo "1. 创建 pom.xml"
echo "2. 创建主启动类 ToolRecommendApplication.java"
echo "3. 创建 application.yml 配置文件"
echo "4. 合并 GlobalExceptionHandler"
echo "5. 修改服务间调用代码(RestTemplate -> 直接注入Service)"
echo "6. 测试运行"
echo ""
echo -e "${YELLOW}单体项目位置:${NC} $MONOLITH_DIR"
