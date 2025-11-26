package com.example.webserial.processor;

import com.example.webserial.entity.SerialDataEntity;
import com.example.webserial.repo.SerialDataRepository;
import com.example.webserial.websocket.WebSocketPushService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SerialDataProcessor {

    private final SerialDataRepository repository;
    private final WebSocketPushService wsService;

    // 通过构造函数注入依赖
    public SerialDataProcessor(SerialDataRepository repository, WebSocketPushService wsService) {
        this.repository = repository;
        this.wsService = wsService;
    }

    // 供 MqttService 调用的实例方法
    public void process(String topic, String raw) {
        try {
            System.out.println("🔄 Processing message - Topic: " + topic + ", Raw: " + raw);
            
            String deviceId = extractDeviceIdFromTopic(topic);
            long timestamp = System.currentTimeMillis();
            Map<String, Object> payload = parseRawPayload(raw);

            System.out.println("   Device ID: " + deviceId);
            System.out.println("   Timestamp: " + timestamp);
            System.out.println("   Payload: " + payload);

            SerialDataEntity entity = new SerialDataEntity();
            entity.setDeviceId(deviceId);
            entity.setTimestamp(timestamp);
            entity.setRawData(raw);
            entity.setPayload(payload);

            // 存库
            System.out.println("💾 Saving to MongoDB...");
            SerialDataEntity saved = repository.save(entity);
            System.out.println("✅ Saved with ID: " + saved.getId());

            // 推送给前端
            System.out.println("📡 Pushing to WebSocket...");
            wsService.sendToClients(entity);
            System.out.println("✅ Process completed");
        } catch (Exception e) {
            System.err.println("❌ Error in process:");
            e.printStackTrace();
        }
    }

    // 话题格式： device/{deviceId}/serial/raw
    private String extractDeviceIdFromTopic(String topic) {
        try {
            String[] parts = topic.split("/");
            if (parts.length >= 2) return parts[1];
        } catch (Exception ignored) {}
        return "unknown";
    }

    // 简单解析示例：支持 "TEMP:24.5;HUM:60" 或 JSON（优先 JSON）
    private Map<String, Object> parseRawPayload(String raw) {
        Map<String, Object> map = new HashMap<>();
        raw = raw.trim();
        // 如果是 JSON
        if (raw.startsWith("{") && raw.endsWith("}")) {
            try {
                // 简单转成 Map（不引用 jackson 库手动解析），但我们这里用 Jackson via Spring util:
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = om.readValue(raw, Map.class);
                return jsonMap;
            } catch (Exception ignored) {}
        }
        // 默认 key:value;key2:value2
        String[] pairs = raw.split(";");
        for (String p : pairs) {
            if (p == null || p.isBlank()) continue;
            String[] kv = p.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim();
                String v = kv[1].trim();
                // 尝试转数字
                Object vv = v;
                try {
                    if (v.contains(".")) vv = Double.parseDouble(v);
                    else vv = Long.parseLong(v);
                } catch (Exception ignored) {}
                map.put(k, vv);
            } else {
                // 非键值结构放入 raw_lines
                map.put(UUID.randomUUID().toString(), p.trim());
            }
        }
        return map;
    }
}
