package com.toolrecommend.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
@MapperScan("com.toolrecommend.user.mapper")
@ComponentScan(basePackages = {"com.toolrecommend.user", "com.toolrecommend.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   User Service 启动成功!");
        System.out.println("   访问地址: http://localhost:8082");
        System.out.println("========================================\n");
    }
}
