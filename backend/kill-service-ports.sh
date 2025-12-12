#!/bin/bash

###############################################################################
# 服务端口清理脚本
# 用途: 在IDEA重启服务前，自动清理占用的端口
# 使用: ./kill-service-ports.sh [service-name]
###############################################################################

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 根据服务名获取端口
get_port_by_service() {
    local service=$1
    case "$service" in
        "tool-service")
            echo "8081"
            ;;
        "user-service")
            echo "8082"
            ;;
        "ai-service")
            echo "8083"
            ;;
        "review-service")
            echo "8084"
            ;;
        "gateway-service")
            echo "8090"
            ;;
        "all")
            echo "8081 8082 8083 8084 8090"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 杀掉指定端口的进程
kill_port() {
    local port=$1
    local pids=$(lsof -ti :$port 2>/dev/null)

    if [ -z "$pids" ]; then
        print_info "端口 $port 未被占用"
        return 0
    fi

    print_warning "端口 $port 被以下进程占用: $pids"

    # 获取进程详情
    for pid in $pids; do
        local process_info=$(ps -p $pid -o comm= 2>/dev/null)
        print_info "  PID: $pid, 进程: $process_info"
    done

    # 尝试优雅关闭 (SIGTERM)
    print_info "尝试优雅关闭端口 $port 的进程..."
    kill $pids 2>/dev/null
    sleep 2

    # 检查进程是否还在
    pids=$(lsof -ti :$port 2>/dev/null)
    if [ -n "$pids" ]; then
        print_warning "进程仍在运行，强制杀掉..."
        kill -9 $pids 2>/dev/null
        sleep 1
    fi

    # 再次检查
    pids=$(lsof -ti :$port 2>/dev/null)
    if [ -z "$pids" ]; then
        print_info "✅ 端口 $port 已释放"
        return 0
    else
        print_error "❌ 无法释放端口 $port"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "======================================"
    echo "    服务端口清理脚本"
    echo "======================================"
    echo ""
    echo "用法: $0 [service-name]"
    echo ""
    echo "可用的服务名称:"
    echo "  tool-service      - 工具服务 (端口 8081)"
    echo "  user-service      - 用户服务 (端口 8082)"
    echo "  ai-service        - AI服务 (端口 8083)"
    echo "  review-service    - 评论服务 (端口 8084)"
    echo "  gateway-service   - 网关服务 (端口 8090)"
    echo "  all               - 清理所有服务端口"
    echo ""
    echo "示例:"
    echo "  $0 user-service   # 清理用户服务端口"
    echo "  $0 all            # 清理所有服务端口"
    echo ""
}

# 主函数
main() {
    local service_name=${1:-"all"}

    echo "======================================"
    echo "    服务端口清理脚本"
    echo "======================================"
    echo ""

    # 获取端口列表
    local ports=$(get_port_by_service "$service_name")

    # 检查服务名是否有效
    if [ -z "$ports" ]; then
        print_error "未知的服务名称: $service_name"
        echo ""
        show_help
        exit 1
    fi

    # 显示要清理的端口
    if [ "$service_name" == "all" ]; then
        print_info "清理所有服务端口: $ports"
    else
        print_info "清理 $service_name 服务端口: $ports"
    fi

    echo ""

    # 清理端口
    local failed=0
    for port in $ports; do
        kill_port $port
        if [ $? -ne 0 ]; then
            ((failed++))
        fi
        echo ""
    done

    # 总结
    echo "======================================"
    if [ $failed -eq 0 ]; then
        print_info "✅ 所有端口已成功清理"
        exit 0
    else
        print_error "❌ 有 $failed 个端口清理失败"
        exit 1
    fi
}

# 执行主函数
main "$@"
