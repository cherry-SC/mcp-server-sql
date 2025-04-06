package com.sc.mcp.server.sql.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SqlFunctionRequest {

    @JsonProperty(required = true, value = "sql")
    @JsonPropertyDescription("SQL查询语句")
    private String sql;

    @JsonProperty(required = true, value = "outputPath")
    @JsonPropertyDescription("查询结果输出文件路径")
    private String outputPath;

    @JsonProperty(value = "database")
    @JsonPropertyDescription("数据库名称，如果不提供则使用默认配置")
    private String database;
}