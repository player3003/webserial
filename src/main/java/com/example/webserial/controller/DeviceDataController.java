package com.example.webserial.controller;

import com.example.webserial.entity.SerialDataEntity;
import com.example.webserial.mqtt.MqttService;
import com.example.webserial.repo.SerialDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceDataController {

    @Autowired
    private SerialDataRepository repository;

    @Autowired
    private MqttService mqttService;

    private final ObjectMapper om = new ObjectMapper();

    @GetMapping("/{deviceId}/data")
    public List<SerialDataEntity> query(
            @PathVariable("deviceId") String deviceId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        System.out.println("🔍 Query request - deviceId: " + deviceId + ", from: " + from + ", to: " + to);
        try {
            List<SerialDataEntity> results = repository.findByDeviceIdAndTimestampBetween(deviceId, from, to);
            System.out.println("✅ Found " + results.size() + " records");
            return results;
        } catch (Exception e) {
            System.err.println("❌ Error in query:");
            e.printStackTrace();
            throw e;
        }
    }

    // 前端 -> 后端 -> 通过 MQTT 下发给设备的命令接口
    @PostMapping("/{deviceId}/command")
    public java.util.Map<String, Object> sendCommand(
            @PathVariable("deviceId") String deviceId,
            @RequestBody(required = true) java.util.Map<String, Object> body) throws Exception {
        String topic = "device/" + deviceId + "/serial/cmd";
        String payload = om.writeValueAsString(body);
        try {
            mqttService.publish(topic, payload, 1, false);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("status", "ok");
            resp.put("topic", topic);
            resp.put("payload", body);
            return resp;
        } catch (MqttException e) {
            System.err.println("❌ Failed to publish command: " + e.getMessage());
            throw e;
        }
    }
}
