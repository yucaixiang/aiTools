package com.toolrecommend.tool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 工具服务启动类
 *
 * @author Tool Recommend Team
 */
@SpringBootApplication
@MapperScan("com.toolrecommend.tool.mapper")
@ComponentScan(basePackages = {"com.toolrecommend.tool", "com.toolrecommend.common"})
public class ToolServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   Tool Service 启动成功!");
        System.out.println("   访问地址: http://localhost:8081");
        System.out.println("========================================\n");
    }
}
