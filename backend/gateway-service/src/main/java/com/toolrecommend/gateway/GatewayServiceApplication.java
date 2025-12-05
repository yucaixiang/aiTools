package com.toolrecommend.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   Gateway Service 启动成功!");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
