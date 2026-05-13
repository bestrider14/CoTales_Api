package com.cotales.cotales_api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock private DataSource dataSource;

    @Mock private org.springframework.core.env.Environment environment;

    @InjectMocks private HealthController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "appName", "TestApp");
    }

    @Test
    void ping_returnsStatusUpWith200() {
        ResponseEntity<Map<String, Object>> response = controller.ping();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void dbHealth_whenConnectionSucceeds_returnsStatusUpWith200() throws Exception {
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.isValid(2)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.dbHealth();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
        assertThat(response.getBody()).containsKey("responseTimeMs");
    }

    @Test
    void dbHealth_whenConnectionFails_returns503WithStatusDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        ResponseEntity<Map<String, Object>> response = controller.dbHealth();

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).containsEntry("status", "DOWN");
        assertThat(response.getBody()).containsEntry("error", "Connection refused");
    }

    @Test
    void info_returnsAllExpectedFields() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});

        ResponseEntity<Map<String, Object>> response = controller.info();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("app", "TestApp");
        assertThat(response.getBody()).containsKey("javaVersion");
        assertThat(response.getBody()).containsKey("uptimeSeconds");
    }

    @Test
    @SuppressWarnings("unchecked")
    void info_whenNoActiveProfiles_returnsDefaultProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});

        ResponseEntity<Map<String, Object>> response = controller.info();

        assertThat((List<String>) response.getBody().get("activeProfiles"))
                .containsExactly("default");
    }

    @Test
    @SuppressWarnings("unchecked")
    void info_whenProfilesActive_returnsActiveProfiles() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

        ResponseEntity<Map<String, Object>> response = controller.info();

        assertThat((List<String>) response.getBody().get("activeProfiles")).containsExactly("dev");
    }
}
