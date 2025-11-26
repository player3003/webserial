package com.example.webserial.mqtt;

import com.example.webserial.processor.SerialDataProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttService implements MqttCallback {

    @Value("${app.mqtt.broker}")
    private String brokerUrl;

    @Value("${app.mqtt.clientId}")
    private String clientId;

    @Value("${app.mqtt.topic}")
    private String topicPattern;

    private MqttClient client;

    @Autowired
    private SerialDataProcessor serialDataProcessor;

    @PostConstruct
    public void init() throws MqttException {
        System.out.println("🔧 Initializing MQTT Service...");
        System.out.println("   Broker: " + brokerUrl);
        System.out.println("   Client ID: " + clientId);
        System.out.println("   Topic Pattern: " + topicPattern);
        
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        // 可按需配置用户名/密码/自动重连等
        client.setCallback(this);
        client.connect(options);

        client.subscribe(topicPattern);
        System.out.println("✅ MQTT connected and subscribed to: " + topicPattern);
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
        // 简单策略：应用重启或实现自动重连
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            System.out.println("📥 MQTT Message Received - Topic: " + topic + ", Payload: " + payload);
            // 委托处理
            serialDataProcessor.process(topic, payload);
            System.out.println("✅ Message processed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error processing MQTT message:");
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used (we don't publish from this client)
    }
}
