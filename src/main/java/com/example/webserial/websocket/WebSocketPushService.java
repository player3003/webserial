package com.example.webserial.websocket;

import com.example.webserial.entity.SerialDataEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketPushService {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendToClients(SerialDataEntity entity) {
        // 构建统一的消息包，包含消息类型以便前端区分
        java.util.Map<String, Object> envelope = new java.util.HashMap<>();
        envelope.put("type", entity.getMessageType() == null ? "telemetry" : entity.getMessageType());
        envelope.put("deviceId", entity.getDeviceId());
        envelope.put("timestamp", entity.getTimestamp());
        envelope.put("rawData", entity.getRawData());
        envelope.put("payload", entity.getPayload());
        envelope.put("correlationId", entity.getCorrelationId());
        envelope.put("meta", entity.getMeta());

        // 广播到全局 topic
        template.convertAndSend("/topic/realtime", envelope);
        // 同时也推送到按设备的 topic，前端可订阅 `/topic/device/{deviceId}`
        if (entity.getDeviceId() != null) {
            String deviceTopic = "/topic/device/" + entity.getDeviceId();
            template.convertAndSend(deviceTopic, envelope);
        }
    }
}