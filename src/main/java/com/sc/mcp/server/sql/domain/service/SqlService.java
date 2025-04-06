package com.sc.mcp.server.sql.domain.service;

import com.sc.mcp.server.sql.domain.model.SqlFunctionRequest;
import com.sc.mcp.server.sql.domain.model.SqlFunctionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SqlService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${spring.datasource.driver-class-name}")
    private String defaultDriverClassName;
    
    @Value("${spring.datasource.url}")
    private String defaultUrl;
    
    @Value("${spring.datasource.username}")
    private String defaultUsername;
    
    @Value("${spring.datasource.password}")
    private String defaultPassword;
    
    /**
     * 根据请求参数创建动态数据源
     * @param request SQL函数请求
     * @return 数据源
     */
    private DataSource createDynamicDataSource(SqlFunctionRequest request) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        
        // 设置驱动类名
        String driverClassName = request.getDriverClassName();
        if (!StringUtils.hasText(driverClassName)) {
            driverClassName = defaultDriverClassName;
        }
        dataSource.setDriverClassName(driverClassName);
        
        // 设置数据库URL
        String url = request.getUrl();
        String database = request.getDatabase();
        
        if (StringUtils.hasText(url)) {
            // 如果提供了完整URL，直接使用
            dataSource.setUrl(url);
        } else if (StringUtils.hasText(database)) {
            // 如果只提供了数据库名称，则构建URL
            // 从默认URL中提取基础连接字符串
            String baseUrl = defaultUrl;
            int dbIndex = baseUrl.indexOf("?");
            if (dbIndex > 0) {
                // 找到第一个问号前的最后一个斜杠位置
                int lastSlashIndex = baseUrl.substring(0, dbIndex).lastIndexOf("/");
                if (lastSlashIndex > 0) {
                    // 替换数据库名称
                    baseUrl = baseUrl.substring(0, lastSlashIndex + 1) + database + baseUrl.substring(dbIndex);
                }
            } else {
                // 如果URL中没有参数部分，直接替换最后一部分
                int lastSlashIndex = baseUrl.lastIndexOf("/");
                if (lastSlashIndex > 0) {
                    baseUrl = baseUrl.substring(0, lastSlashIndex + 1) + database;
                }
            }
            dataSource.setUrl(baseUrl);
            log.info("使用数据库: {}, 构建URL: {}", database, baseUrl);
        } else {
            // 如果没有提供URL和数据库名称，使用默认URL
            dataSource.setUrl(defaultUrl);
        }
        
        // 设置用户名
        String username = request.getUsername();
        if (!StringUtils.hasText(username)) {
            username = defaultUsername;
        }
        dataSource.setUsername(username);
        
        // 设置密码
        String password = request.getPassword();
        if (!StringUtils.hasText(password)) {
            password = defaultPassword;
        }
        dataSource.setPassword(password);
        
        log.info("创建动态数据源:, url={}, username={}",
                dataSource.getUrl(), 
                dataSource.getUsername());
        
        return dataSource;
    }

    @Tool(description = "执行SQL查询并将结果写入文件")
    public SqlFunctionResponse executeQuery(SqlFunctionRequest request) {
        log.info("执行SQL查询: {}", request.getSql());
        long startTime = System.currentTimeMillis();
        
        SqlFunctionResponse response = new SqlFunctionResponse();
        response.setSuccess(false);
        response.setOutputFilePath(request.getOutputPath());
        
        try {
            // 创建动态数据源和JdbcTemplate
            DataSource dynamicDataSource = createDynamicDataSource(request);
            // 每次查询都创建新的JdbcTemplate实例，确保使用正确的数据源
            JdbcTemplate dynamicJdbcTemplate = new JdbcTemplate(dynamicDataSource);
            
            // 执行查询
            List<Map<String, Object>> results = dynamicJdbcTemplate.queryForList(request.getSql());
            
            // 创建输出文件目录
            File outputFile = new File(request.getOutputPath());
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 写入查询结果到文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                // 写入列头
                if (!results.isEmpty()) {
                    Map<String, Object> firstRow = results.get(0);
                    boolean isFirst = true;
                    for (String columnName : firstRow.keySet()) {
                        if (!isFirst) {
                            writer.write(",");
                        }
                        writer.write(columnName);
                        isFirst = false;
                    }
                    writer.newLine();
                }
                
                // 写入数据行
                for (Map<String, Object> row : results) {
                    boolean isFirst = true;
                    for (Object value : row.values()) {
                        if (!isFirst) {
                            writer.write(",");
                        }
                        writer.write(value != null ? value.toString() : "NULL");
                        isFirst = false;
                    }
                    writer.newLine();
                }
            }
            
            long endTime = System.currentTimeMillis();
            response.setSuccess(true);
            response.setMessage("查询执行成功，结果已写入文件");
            response.setRowsAffected(results.size());
            response.setExecutionTime(endTime - startTime);
            
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            response.setMessage("执行SQL查询失败");
            response.setError(e.getMessage());
            response.setRowsAffected(0);
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }
        
        return response;
    }
    
    @Tool(description = "执行SQL更新操作(INSERT, UPDATE, DELETE)并返回结果")
    public SqlFunctionResponse executeUpdate(SqlFunctionRequest request) {
        log.info("执行SQL更新操作: {}", request.getSql());
        long startTime = System.currentTimeMillis();
        
        SqlFunctionResponse response = new SqlFunctionResponse();
        response.setSuccess(false);
        response.setOutputFilePath(request.getOutputPath());
        
        try {
            // 创建动态数据源和JdbcTemplate
            DataSource dynamicDataSource = createDynamicDataSource(request);
            // 每次更新操作都创建新的JdbcTemplate实例，确保使用正确的数据源
            JdbcTemplate dynamicJdbcTemplate = new JdbcTemplate(dynamicDataSource);
            
            // 执行更新操作
            int rowsAffected = dynamicJdbcTemplate.update(request.getSql());
            
            // 创建输出文件目录
            File outputFile = new File(request.getOutputPath());
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 写入执行结果到文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write("SQL执行结果");
                writer.newLine();
                writer.write("影响行数: " + rowsAffected);
                writer.newLine();
                writer.write("执行时间: " + (System.currentTimeMillis() - startTime) + "ms");
            }
            
            long endTime = System.currentTimeMillis();
            response.setSuccess(true);
            response.setMessage("SQL更新操作执行成功");
            response.setRowsAffected(rowsAffected);
            response.setExecutionTime(endTime - startTime);
            
        } catch (Exception e) {
            log.error("执行SQL更新操作失败", e);
            response.setMessage("执行SQL更新操作失败");
            response.setError(e.getMessage());
            response.setRowsAffected(0);
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }
        
        return response;
    }
}