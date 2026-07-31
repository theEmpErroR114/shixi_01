package com.examsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 课程习题测验系统 — Spring Boot 主启动类。
 * <p>
 * 职责：
 * <ul>
 *   <li>启动 Spring Boot 内嵌 Tomcat 服务器（默认端口 8080）</li>
 *   <li>通过 {@code @MapperScan} 自动扫描 MyBatis Mapper 接口并生成代理实现</li>
 *   <li>触发 {@code CommandLineRunner}（如 {@link com.examsystem.config.DataInitializer}）执行初始化逻辑</li>
 * </ul>
 *
 * @see com.examsystem.config.DataInitializer 数据库种子数据初始化
 */
@SpringBootApplication
@MapperScan("com.examsystem.mapper") // 扫描 MyBatis Mapper 接口所在包，自动注册为 Spring Bean
public class ExamSystemApplication {
    /**
     * 应用程序入口。Spring Boot 会自动完成 IoC 容器初始化、组件扫描、内嵌服务器启动等工作。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ExamSystemApplication.class, args);
    }
}
