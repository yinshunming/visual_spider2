package com.visualspider.health;

import com.visualspider.persistence.DatabaseProbeMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DatabaseProbeMapper databaseProbeMapper;

    public HealthController(DatabaseProbeMapper databaseProbeMapper) {
        this.databaseProbeMapper = databaseProbeMapper;
    }

    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> healthz() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("application", "visual-spider2");
        payload.put("checkedAt", Instant.now().toString());

        try {
            payload.put("database", databaseProbeMapper.selectOne() == 1 ? "UP" : "DOWN");
            payload.put("status", "UP");
            return ResponseEntity.ok(payload);
        } catch (Exception ex) {
            payload.put("database", "DOWN");
            payload.put("status", "DOWN");
            payload.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(payload);
        }
    }
}

