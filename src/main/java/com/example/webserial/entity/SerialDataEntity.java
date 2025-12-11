package com.example.webserial.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "serial_data")
public class SerialDataEntity {
    @Id
    private String id;

    private String deviceId;
    private long timestamp;
    private String rawData;
    private Map<String, Object> payload;

    // 新增：消息类型（telemetry/status/command/ack/other）
    private String messageType;

    // 可选的相关 ID，用于命令和应答关联
    private String correlationId;

    // 可选元数据
    private Map<String, Object> meta;

    // 可选顺序号
    private Long sequence;
}
