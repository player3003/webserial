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
}
