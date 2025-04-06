package com.sc.mcp.server.sql;

import com.sc.mcp.server.sql.domain.service.SqlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author sc
 * @description SQL服务应用程序入口
 * @create 2025/4/6 14:31
 */
@SpringBootApplication
@Slf4j
public class McpServerSqlApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(McpServerSqlApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider sqlTools(SqlService sqlService){
        return MethodToolCallbackProvider.builder().toolObjects(sqlService).build();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("mcp-server-sql start success");
    }
}