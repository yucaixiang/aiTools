package com.toolrecommend.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 评论服务启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
@MapperScan("com.toolrecommend.review.mapper")
@ComponentScan(basePackages = {"com.toolrecommend.review", "com.toolrecommend.common"})
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   Review Service 启动成功!");
        System.out.println("   访问地址: http://localhost:8084");
        System.out.println("========================================\n");
    }
}
