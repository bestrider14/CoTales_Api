package com.cotales.cotales_api.user.user;

import java.time.OffsetDateTime;

public record UserResponse(Long id, String username, String email, OffsetDateTime createdAt) {}
