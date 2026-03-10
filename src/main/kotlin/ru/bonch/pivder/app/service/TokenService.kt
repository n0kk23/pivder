package ru.bonch.pivder.app.service

import ru.bonch.pivder.app.dto.response.TokenResponseDto
import ru.bonch.pivder.app.entity.AccountEntity

interface TokenService {
    fun generateRefreshToken(account: AccountEntity): String
    fun generateAccessToken(account: AccountEntity): String
    fun refreshTokens(refreshToken: String): TokenResponseDto
}