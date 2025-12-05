package com.toolrecommend.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI服务启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
@MapperScan("com.toolrecommend.ai.mapper")
@ComponentScan(basePackages = {"com.toolrecommend.ai", "com.toolrecommend.common"})
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   AI Service 启动成功!");
        System.out.println("   访问地址: http://localhost:8083");
        System.out.println("========================================\n");
    }
}
