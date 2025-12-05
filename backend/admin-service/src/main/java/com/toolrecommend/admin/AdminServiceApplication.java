package com.toolrecommend.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Admin Service 启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication(scanBasePackages = {"com.toolrecommend.admin", "com.toolrecommend.common"})
@MapperScan("com.toolrecommend.admin.mapper")
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("Admin Service started successfully!");
        System.out.println("Port: 8085");
        System.out.println("========================================");
    }
}
