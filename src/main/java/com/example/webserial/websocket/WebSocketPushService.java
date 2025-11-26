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
        // 发送到 /topic/realtime，前端订阅该 topic 即可收到
        template.convertAndSend("/topic/realtime", entity);
    }
}