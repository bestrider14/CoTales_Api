package com.cotales.cotales_api.common;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/health")
public class HealthController extends ApiController {

    private final DataSource dataSource;
    private final Environment environment;

    @Value("${spring.application.name}")
    private String appName;

    /** GET /api/v1/health — vérifie que l'app tourne */
    @GetMapping
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", Instant.now().toString()));
    }

    /** GET /api/v1/health/db — teste la connexion à la base de données */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(2);
            return ResponseEntity.ok(
                    Map.of("status", "UP", "responseTimeMs", System.currentTimeMillis() - start));
        } catch (SQLException e) {
            return ResponseEntity.status(503)
                    .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }

    /** GET /api/v1/health/info — infos de l'app (profil actif, uptime, version Java) */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        return ResponseEntity.ok(
                Map.of(
                        "app",
                        appName,
                        "javaVersion",
                        System.getProperty("java.version"),
                        "activeProfiles",
                        profiles.isEmpty() ? List.of("default") : profiles,
                        "uptimeSeconds",
                        uptimeSeconds));
    }
}
