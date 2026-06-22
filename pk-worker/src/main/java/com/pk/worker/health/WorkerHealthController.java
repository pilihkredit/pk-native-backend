package com.pk.worker.health;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerHealthController {
    @GetMapping("/worker/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "component", "pk-worker",
                "time", Instant.now().toString()
        );
    }
}
