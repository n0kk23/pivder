package ru.bonch.pivder.app.dto.request

import jakarta.validation.constraints.NotBlank

class RefreshTokenRequestDto(
    @field:NotBlank
    val refreshToken: String
)