package com.cotales.cotales_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    // 512-bit Base64-encoded secret for tests
    private static final String SECRET =
            "fAC2JQwzHOT8e0Z25Mj-dBd3wkFxXLO26VCfocLjcKIDZgvNO0DXVtPm4WEVFdvl8r1PD0zi5sp28zXrRIYq5Q";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 900, 604800, false);
        userDetails =
                User.withUsername("user@mail.com")
                        .password("hashed")
                        .authorities("ROLE_USER")
                        .build();
    }

    @Test
    void generateToken_producesNonNullToken() {
        assertThat(jwtService.generateToken(userDetails)).isNotBlank();
    }

    @Test
    void generateRefreshToken_producesNonNullToken() {
        assertThat(jwtService.generateRefreshToken(userDetails)).isNotBlank();
    }

    @Test
    void generateToken_andRefreshToken_areDifferent() {
        assertThat(jwtService.generateToken(userDetails))
                .isNotEqualTo(jwtService.generateRefreshToken(userDetails));
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@mail.com");
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withWrongUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser =
                User.withUsername("other@mail.com").password("x").authorities("ROLE_USER").build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        JwtService shortLivedService = new JwtService(SECRET, -1, 604800, false);
        String token = shortLivedService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_returnsFalse() {
        String token = jwtService.generateToken(userDetails) + "tampered";

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void extractUsername_withInvalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getAccessTokenExpiration_returns900() {
        assertThat(jwtService.getAccessTokenExpiration()).isEqualTo(900);
    }
}
