package com.example.webserial.repo;

import com.example.webserial.entity.SerialDataEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SerialDataRepository extends MongoRepository<SerialDataEntity, String> {
    List<SerialDataEntity> findByDeviceIdAndTimestampBetween(String deviceId, long from, long to);
}
