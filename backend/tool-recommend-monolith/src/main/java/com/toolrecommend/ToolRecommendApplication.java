package com.toolrecommend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Tool Recommend 单体应用主启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
@MapperScan("com.toolrecommend.*.mapper")  // 通配符路径可能导致问题
public class ToolRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolRecommendApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   Tool Recommend 单体应用启动成功!");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
