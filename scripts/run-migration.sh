#!/bin/bash

# 数据库迁移执行脚本
# 用法: ./run-migration.sh

set -e

echo "=========================================="
echo "      评分功能数据库迁移脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# MySQL配置
DB_HOST="localhost"
DB_NAME="tool_recommend"
DB_USER="root"

# 脚本路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATION_SCRIPT="${SCRIPT_DIR}/migrate-rating-review.sql"
BACKUP_DIR="${SCRIPT_DIR}/../backups"

# 创建备份目录
mkdir -p "${BACKUP_DIR}"

echo -e "${YELLOW}⚠️  注意事项:${NC}"
echo "1. 此脚本会修改数据库表结构"
echo "2. 执行前会自动备份数据库"
echo "3. 如有问题可以使用备份恢复"
echo ""

# 询问是否继续
read -p "是否继续执行迁移? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo -e "${RED}❌ 已取消迁移${NC}"
    exit 0
fi

# 输入MySQL密码
echo ""
echo -e "${GREEN}步骤1/4: 连接数据库${NC}"
read -sp "请输入MySQL root密码: " DB_PASSWORD
echo ""

# 测试数据库连接
echo -e "${GREEN}步骤2/4: 测试数据库连接${NC}"
mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "USE ${DB_NAME};" 2>/dev/null
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 数据库连接失败，请检查密码是否正确${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 数据库连接成功${NC}"

# 备份数据库
BACKUP_FILE="${BACKUP_DIR}/backup_$(date +%Y%m%d_%H%M%S).sql"
echo ""
echo -e "${GREEN}步骤3/4: 备份数据库${NC}"
echo "备份位置: ${BACKUP_FILE}"
mysqldump -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" > "${BACKUP_FILE}" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库备份成功 ($(du -h "${BACKUP_FILE}" | cut -f1))${NC}"
else
    echo -e "${RED}❌ 数据库备份失败${NC}"
    exit 1
fi

# 执行迁移
echo ""
echo -e "${GREEN}步骤4/4: 执行数据库迁移${NC}"
mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${MIGRATION_SCRIPT}" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库迁移成功${NC}"
else
    echo -e "${RED}❌ 数据库迁移失败${NC}"
    echo -e "${YELLOW}正在恢复备份...${NC}"
    mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${BACKUP_FILE}" 2>/dev/null
    echo -e "${GREEN}✓ 数据库已恢复${NC}"
    exit 1
fi

# 验证迁移结果
echo ""
echo -e "${GREEN}验证迁移结果:${NC}"

# 检查rating表
RATING_EXISTS=$(mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "SHOW TABLES LIKE 'rating';" 2>/dev/null | grep -c "rating")
if [ "$RATING_EXISTS" -eq 1 ]; then
    echo -e "${GREEN}✓ rating表创建成功${NC}"
    RATING_COUNT=$(mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "SELECT COUNT(*) FROM rating;" 2>/dev/null | tail -n 1)
    echo "  - 评分记录数: ${RATING_COUNT}"
else
    echo -e "${RED}✗ rating表未找到${NC}"
fi

# 检查review表字段
PARENT_ID_EXISTS=$(mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "DESC review;" 2>/dev/null | grep -c "parent_id")
if [ "$PARENT_ID_EXISTS" -eq 1 ]; then
    echo -e "${GREEN}✓ review表已添加parent_id字段${NC}"
else
    echo -e "${RED}✗ review表parent_id字段未找到${NC}"
fi

REPLY_COUNT_EXISTS=$(mysql -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "DESC review;" 2>/dev/null | grep -c "reply_count")
if [ "$REPLY_COUNT_EXISTS" -eq 1 ]; then
    echo -e "${GREEN}✓ review表已添加reply_count字段${NC}"
else
    echo -e "${RED}✗ review表reply_count字段未找到${NC}"
fi

echo ""
echo -e "${GREEN}=========================================="
echo "          迁移完成！"
echo "==========================================${NC}"
echo ""
echo "备份文件位置: ${BACKUP_FILE}"
echo ""
echo "下一步:"
echo "1. 重启后端服务(如果正在运行)"
echo "2. 测试评分功能"
echo ""
