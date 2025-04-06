package com.sc.mcp.server.sql.domain.service;

import com.sc.mcp.server.sql.domain.model.SqlFunctionRequest;
import com.sc.mcp.server.sql.domain.model.SqlFunctionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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

    @Tool(description = "执行SQL查询并将结果写入文件")
    public SqlFunctionResponse executeQuery(SqlFunctionRequest request) {
        log.info("执行SQL查询: {}", request.getSql());
        long startTime = System.currentTimeMillis();
        
        SqlFunctionResponse response = new SqlFunctionResponse();
        response.setSuccess(false);
        response.setOutputFilePath(request.getOutputPath());
        
        try {
            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(request.getSql());
            
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
            // 执行更新操作
            int rowsAffected = jdbcTemplate.update(request.getSql());
            
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