#!/bin/bash

# 执行数据库迁移脚本
# 使用方法: ./execute-migration.sh

echo "执行数据库迁移脚本..."

# 方法1: 如果你的MySQL客户端正常工作
# mysql -h127.0.0.1 -P3306 -uroot -p123456 tool_recommend < migrations/V5__Create_Favorite_And_Submission_Tables.sql

# 方法2: 手动执行SQL
echo "请手动在MySQL客户端中执行以下SQL语句:"
echo "-------------------------------------------"
cat migrations/V5__Create_Favorite_And_Submission_Tables.sql
echo "-------------------------------------------"

echo ""
echo "或者直接复制以下命令执行:"
echo "USE tool_recommend;"
echo "然后复制粘贴SQL文件内容到MySQL客户端执行"
