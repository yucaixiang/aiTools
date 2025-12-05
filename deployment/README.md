# 部署文档

## 快速启动

### 1. 启动所有基础服务

```bash
cd deployment
docker-compose up -d
```

### 2. 检查服务状态

```bash
docker-compose ps
```

所有服务应该显示为`Up`状态。

### 3. 服务访问地址

| 服务 | 地址 | 用户名 | 密码 |
|------|------|--------|------|
| MySQL | localhost:3306 | root | root123456 |
| Redis | localhost:6379 | - | redis123456 |
| Elasticsearch | http://localhost:9200 | - | - |
| Kibana | http://localhost:5601 | - | - |
| Qdrant | http://localhost:6333 | - | - |
| Ollama | http://localhost:11434 | - | - |
| Kafka | localhost:9092 | - | - |
| MinIO | http://localhost:9000 | admin | admin123456 |
| MinIO Console | http://localhost:9001 | admin | admin123456 |
| Portainer | https://localhost:9443 | 首次访问设置 | - |

### 4. 初始化数据库

```bash
# 导入数据库schema
docker exec -i tool-recommend-mysql mysql -uroot -proot123456 tool_recommend < ../scripts/init-db.sql

# 导入示例数据（可选）
docker exec -i tool-recommend-mysql mysql -uroot -proot123456 tool_recommend < ../scripts/init-data.sql
```

### 5. 拉取AI模型（Ollama）

```bash
# 拉取Qwen2.5模型（用于对话）
docker exec -it tool-recommend-ollama ollama pull qwen2.5:7b

# 拉取BGE模型（用于向量化）
docker exec -it tool-recommend-ollama ollama pull bge-large-zh

# 验证模型
docker exec -it tool-recommend-ollama ollama list
```

## 服务管理命令

### 启动服务
```bash
docker-compose up -d
```

### 停止服务
```bash
docker-compose down
```

### 重启服务
```bash
docker-compose restart
```

### 查看日志
```bash
# 查看所有日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f mysql
docker-compose logs -f ollama
```

### 停止并删除所有数据
```bash
docker-compose down -v
```

## 性能配置

### MySQL优化
编辑`mysql/conf/my.cnf`：
```ini
[mysqld]
max_connections=1000
innodb_buffer_pool_size=2G
slow_query_log=1
long_query_time=1
```

### Elasticsearch优化
调整Docker Compose中的内存设置：
```yaml
elasticsearch:
  environment:
    - ES_JAVA_OPTS=-Xms2g -Xmx2g
```

## 故障排查

### MySQL连接失败
```bash
# 检查MySQL是否启动
docker ps | grep mysql

# 查看MySQL日志
docker logs tool-recommend-mysql

# 进入MySQL容器
docker exec -it tool-recommend-mysql mysql -uroot -proot123456
```

### Ollama模型加载慢
```bash
# 检查模型是否下载完成
docker exec -it tool-recommend-ollama ollama list

# 手动下载模型
docker exec -it tool-recommend-ollama ollama pull qwen2.5:7b
```

### 端口冲突
修改`docker-compose.yml`中的端口映射，例如：
```yaml
mysql:
  ports:
    - "3307:3306"  # 改为3307端口
```

## 生产环境部署

生产环境建议使用Kubernetes部署，详见《K8s部署文档.md》。

## 备份和恢复

### 数据库备份
```bash
docker exec tool-recommend-mysql mysqldump -uroot -proot123456 tool_recommend > backup.sql
```

### 数据库恢复
```bash
docker exec -i tool-recommend-mysql mysql -uroot -proot123456 tool_recommend < backup.sql
```

## 监控

### Portainer管理
访问 https://localhost:9443 使用Portainer可视化管理Docker容器。

### 日志查看
```bash
# 实时查看所有日志
docker-compose logs -f

# 查看最近100行日志
docker-compose logs --tail=100
```

## 常见问题

**Q: 服务启动失败？**
A: 检查端口是否被占用，查看日志排查错误。

**Q: Ollama内存不足？**
A: 修改docker-compose.yml，增加内存限制或使用更小的模型。

**Q: 数据持久化问题？**
A: 确保volumes正确配置，避免使用`docker-compose down -v`删除数据。
