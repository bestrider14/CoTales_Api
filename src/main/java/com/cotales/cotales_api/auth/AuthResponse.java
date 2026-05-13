package com.cotales.cotales_api.auth;

public record AuthResponse(String accessToken, long expiresIn) {}
