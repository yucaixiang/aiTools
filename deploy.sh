#!/bin/bash

# AI工具推荐系统 - 一键部署脚本
# 用于快速部署整个系统到生产环境

set -e  # 遇到错误立即退出

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_DIR="/Users/bjsttlp324/Desktop/tools"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend/uniapp"
DEPLOYMENT_DIR="$PROJECT_DIR/deployment"

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_header() {
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo ""
}

# 检查Docker是否运行
check_docker() {
    print_header "检查Docker环境"

    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        print_error "Docker未运行，请启动Docker"
        exit 1
    fi

    print_success "Docker环境正常"
}

# 检查Maven是否安装
check_maven() {
    print_header "检查Maven环境"

    if ! command -v mvn &> /dev/null; then
        print_error "Maven未安装，请先安装Maven"
        exit 1
    fi

    print_success "Maven环境正常"
}

# 检查Node.js是否安装
check_node() {
    print_header "检查Node.js环境"

    if ! command -v node &> /dev/null; then
        print_error "Node.js未安装，请先安装Node.js"
        exit 1
    fi

    if ! command -v npm &> /dev/null; then
        print_error "npm未安装，请先安装npm"
        exit 1
    fi

    print_success "Node.js环境正常"
}

# 启动基础服务
start_infrastructure() {
    print_header "启动基础服务"

    cd "$DEPLOYMENT_DIR"

    print_info "启动Docker Compose服务..."
    docker-compose up -d mysql redis elasticsearch ollama

    print_info "等待服务启动..."
    sleep 10

    # 检查MySQL
    print_info "检查MySQL连接..."
    for i in {1..30}; do
        if docker exec tool-recommend-mysql mysqladmin ping -h localhost --silent &> /dev/null; then
            print_success "MySQL已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "MySQL启动超时"
            exit 1
        fi
        sleep 2
    done

    # 检查Redis
    print_info "检查Redis连接..."
    if docker exec tool-recommend-redis redis-cli ping &> /dev/null; then
        print_success "Redis已就绪"
    else
        print_error "Redis启动失败"
        exit 1
    fi

    print_success "所有基础服务启动完成"
}

# 初始化数据库
init_database() {
    print_header "初始化数据库"

    print_info "执行数据库初始化脚本..."
    docker exec -i tool-recommend-mysql mysql -uroot -proot123456 < "$PROJECT_DIR/scripts/init-db.sql"

    print_info "导入示例数据..."
    docker exec -i tool-recommend-mysql mysql -uroot -proot123456 tool_recommend < "$PROJECT_DIR/scripts/init-sample-data.sql"

    print_success "数据库初始化完成"
}

# 编译后端服务
build_backend() {
    print_header "编译后端服务"

    cd "$BACKEND_DIR"

    print_info "清理旧的构建..."
    mvn clean -q

    print_info "开始Maven编译..."
    mvn package -DskipTests -q

    if [ $? -eq 0 ]; then
        print_success "后端编译成功"
    else
        print_error "后端编译失败"
        exit 1
    fi
}

# 启动后端服务
start_backend() {
    print_header "启动后端服务"

    print_info "启动服务的顺序很重要，请按以下顺序启动："
    echo ""
    echo "1. Tool Service (8081)"
    echo "2. User Service (8082)"
    echo "3. AI Service (8083)"
    echo "4. Review Service (8084)"
    echo "5. Admin Service (8085)"
    echo "6. Gateway Service (8080) - 最后启动"
    echo ""

    print_warning "请使用IntelliJ IDEA或以下命令手动启动服务："
    echo ""
    echo "cd $BACKEND_DIR/tool-service && mvn spring-boot:run &"
    echo "cd $BACKEND_DIR/user-service && mvn spring-boot:run &"
    echo "cd $BACKEND_DIR/ai-service && mvn spring-boot:run &"
    echo "cd $BACKEND_DIR/review-service && mvn spring-boot:run &"
    echo "cd $BACKEND_DIR/admin-service && mvn spring-boot:run &"
    echo "cd $BACKEND_DIR/gateway-service && mvn spring-boot:run &"
    echo ""
}

# 编译前端
build_frontend() {
    print_header "编译前端应用"

    cd "$FRONTEND_DIR"

    if [ ! -d "node_modules" ]; then
        print_info "安装npm依赖..."
        npm install
    fi

    print_info "编译H5版本..."
    npm run build:h5

    if [ $? -eq 0 ]; then
        print_success "前端编译成功"
    else
        print_error "前端编译失败"
        exit 1
    fi
}

# 健康检查
health_check() {
    print_header "系统健康检查"

    local services=(
        "Gateway:http://localhost:8080/actuator/health"
        "Tool:http://localhost:8081/actuator/health"
        "User:http://localhost:8082/actuator/health"
        "AI:http://localhost:8083/actuator/health"
        "Review:http://localhost:8084/actuator/health"
        "Admin:http://localhost:8085/actuator/health"
    )

    for service in "${services[@]}"; do
        IFS=':' read -r name url <<< "$service"

        print_info "检查 $name Service..."

        response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

        if [ "$response" = "200" ]; then
            print_success "$name Service 运行正常"
        else
            print_warning "$name Service 未响应 (HTTP $response)"
        fi
    done
}

# 显示访问信息
show_access_info() {
    print_header "部署完成"

    print_success "系统部署成功！"
    echo ""
    echo -e "${BLUE}访问信息:${NC}"
    echo ""
    echo "  API网关:     http://localhost:8080"
    echo "  前端H5:      http://localhost:3000"
    echo "  MySQL:       localhost:3306"
    echo "  Redis:       localhost:6379"
    echo "  Elasticsearch: http://localhost:9200"
    echo "  Kibana:      http://localhost:5601"
    echo ""
    echo -e "${BLUE}服务状态:${NC}"
    echo ""
    echo "  Gateway Service:  8080"
    echo "  Tool Service:     8081"
    echo "  User Service:     8082"
    echo "  AI Service:       8083"
    echo "  Review Service:   8084"
    echo "  Admin Service:    8085"
    echo ""
    echo -e "${BLUE}快速测试:${NC}"
    echo ""
    echo "  # 查询热门工具"
    echo "  curl http://localhost:8080/api/tools/hot"
    echo ""
    echo "  # 运行测试脚本"
    echo "  ./test-api.sh"
    echo ""
    print_success "祝使用愉快！🎉"
}

# 主函数
main() {
    print_header "AI工具推荐系统 - 一键部署"

    echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""

    # 1. 环境检查
    check_docker
    check_maven
    check_node

    # 2. 启动基础服务
    start_infrastructure

    # 3. 初始化数据库
    init_database

    # 4. 编译后端
    build_backend

    # 5. 启动后端服务
    start_backend

    # 6. 编译前端
    build_frontend

    # 7. 显示访问信息
    show_access_info

    echo ""
    echo "结束时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""

    print_info "提示: 后端服务需要手动启动，请参考上面的说明"
    print_info "可以运行 ./test-api.sh 来测试API接口"
}

# 执行主函数
main "$@"
