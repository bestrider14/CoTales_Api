package com.cotales.cotales_api.auth;

import com.cotales.cotales_api.common.exception.UnauthorizedException;
import com.cotales.cotales_api.security.JwtService;
import com.cotales.cotales_api.security.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        setRefreshTokenCookie(response, jwtService.generateRefreshToken(userDetails));
        return new AuthResponse(
                jwtService.generateToken(userDetails), jwtService.getAccessTokenExpiration());
    }

    public AuthResponse refresh(String refreshToken, HttpServletResponse response) {
        try {
            String email = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }
            setRefreshTokenCookie(response, jwtService.generateRefreshToken(userDetails));
            return new AuthResponse(
                    jwtService.generateToken(userDetails), jwtService.getAccessTokenExpiration());
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", token)
                        .httpOnly(true)
                        .secure(jwtService.isProduction())
                        .path("/api/v1/auth/refresh")
                        .maxAge(jwtService.getRefreshTokenExpiration())
                        .sameSite("Lax")
                        .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
