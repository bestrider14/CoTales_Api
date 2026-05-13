package com.cotales.cotales_api.auth;

import com.cotales.cotales_api.common.exception.NotFoundException;
import com.cotales.cotales_api.common.exception.UnauthorizedException;
import com.cotales.cotales_api.security.JwtService;
import com.cotales.cotales_api.user.user.User;
import com.cotales.cotales_api.user.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user =
                userRepository
                        .findByEmail(request.email())
                        .orElseThrow(() -> new NotFoundException("User not found"));

        return buildAuthResponse(user, response);
    }

    public AuthResponse refresh(String refreshToken, HttpServletResponse response) {
        try {
            if (!jwtService.isTokenValid(refreshToken)) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }
            User user =
                    userRepository
                            .findById(jwtService.extractUserId(refreshToken))
                            .orElseThrow(() -> new UnauthorizedException("User not found"));

            return buildAuthResponse(user, response);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    private AuthResponse buildAuthResponse(User user, HttpServletResponse response) {
        setRefreshTokenCookie(
                response,
                jwtService.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail()));
        return new AuthResponse(
                jwtService.generateToken(user.getId(), user.getUsername(), user.getEmail()),
                jwtService.getAccessTokenExpiration());
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
