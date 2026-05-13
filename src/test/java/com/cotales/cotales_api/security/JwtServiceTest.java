package com.cotales.cotales_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET =
            "fAC2JQwzHOT8e0Z25Mj-dBd3wkFxXLO26VCfocLjcKIDZgvNO0DXVtPm4WEVFdvl8r1PD0zi5sp28zXrRIYq5Q";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret(SECRET);
        config.setAccessTokenExpiration(900);
        config.setRefreshTokenExpiration(604800);
        config.setIsProduction(false);
        jwtService = new JwtService(config);
    }

    @Test
    void generateToken_producesNonNullToken() {
        assertThat(jwtService.generateToken(1L, "luka", "user@mail.com")).isNotBlank();
    }

    @Test
    void generateRefreshToken_producesNonNullToken() {
        assertThat(jwtService.generateRefreshToken(1L, "luka", "user@mail.com")).isNotBlank();
    }

    @Test
    void generateToken_andRefreshToken_areDifferent() {
        assertThat(jwtService.generateToken(1L, "luka", "user@mail.com"))
                .isNotEqualTo(jwtService.generateRefreshToken(1L, "luka", "user@mail.com"));
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtService.generateToken(42L, "luka", "user@mail.com");

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateToken(1L, "luka", "user@mail.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@mail.com");
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(1L, "luka", "user@mail.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        JwtConfig shortConfig = new JwtConfig();
        shortConfig.setSecret(SECRET);
        shortConfig.setAccessTokenExpiration(-1);
        shortConfig.setRefreshTokenExpiration(604800);
        shortConfig.setIsProduction(false);
        JwtService shortLivedService = new JwtService(shortConfig);
        String token = shortLivedService.generateToken(1L, "luka", "user@mail.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_returnsFalse() {
        String token = jwtService.generateToken(1L, "luka", "user@mail.com") + "tampered";

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void extractUserId_withInvalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUserId("not.a.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getAccessTokenExpiration_returns900() {
        assertThat(jwtService.getAccessTokenExpiration()).isEqualTo(900);
    }
}
