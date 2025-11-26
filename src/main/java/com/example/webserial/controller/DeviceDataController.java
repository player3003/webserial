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
            @PathVariable String deviceId,
            @RequestParam long from,
            @RequestParam long to) {
        return repository.findByDeviceIdAndTimestampBetween(deviceId, from, to);
    }
}
