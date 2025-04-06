package com.sc.mcp.server.sql.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SqlFunctionResponse {

    @JsonProperty(required = true, value = "success")
    @JsonPropertyDescription("查询是否成功")
    private boolean success;

    @JsonProperty(required = true, value = "message")
    @JsonPropertyDescription("执行结果消息")
    private String message;

    @JsonProperty(required = true, value = "rowsAffected")
    @JsonPropertyDescription("影响的行数")
    private int rowsAffected;

    @JsonProperty(required = true, value = "outputFilePath")
    @JsonPropertyDescription("结果输出文件路径")
    private String outputFilePath;

    @JsonProperty(value = "executionTime")
    @JsonPropertyDescription("查询执行时间(毫秒)")
    private long executionTime;

    @JsonProperty(value = "error")
    @JsonPropertyDescription("错误信息，如果有的话")
    private String error;
}