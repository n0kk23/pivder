package ru.bonch.pivder.app.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.bonch.pivder.app.dto.request.RefreshTokenRequestDto
import ru.bonch.pivder.app.dto.response.TokenResponseDto
import ru.bonch.pivder.app.service.TokenService

@RestController
@RequestMapping("/api/v1/refresh")
class RefreshTokenController(
    private val tokenService: TokenService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(
        @RequestBody @Valid request: RefreshTokenRequestDto
    ): TokenResponseDto {
        return tokenService.refreshTokens(request.refreshToken)
    }
}