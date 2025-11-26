package com.example.webserial.controller;

import com.example.webserial.entity.SerialDataEntity;
import com.example.webserial.repo.SerialDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceDataController {

    @Autowired
    private SerialDataRepository repository;

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
}
