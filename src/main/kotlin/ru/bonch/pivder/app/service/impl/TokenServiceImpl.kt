package ru.bonch.pivder.app.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.bonch.pivder.app.dto.response.TokenResponseDto
import ru.bonch.pivder.app.entity.AccountEntity
import ru.bonch.pivder.app.entity.RefreshTokenEntity
import ru.bonch.pivder.app.exception.notfound.impl.TokenNotFoundException
import ru.bonch.pivder.app.exception.unauthorized.impl.InvalidTokenException
import ru.bonch.pivder.app.jwt.JwtProvider
import ru.bonch.pivder.app.repository.RefreshTokenRepository
import ru.bonch.pivder.app.service.TokenService
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class TokenServiceImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider
) : TokenService {

    companion object {
        const val HASH_ALGORITHM: String = "SHA-256"
        const val ACCOUNT_ID_MUST_BE_NOT_NULL = "Account id must be not null"
    }

    @Transactional
    override fun generateRefreshToken(account: AccountEntity): String {
        val userId: UUID = requireNotNull(account.id) { ACCOUNT_ID_MUST_BE_NOT_NULL }
        val token = jwtProvider.generateRefreshToken(userId)

        val expiresAt: OffsetDateTime = jwtProvider.extractExpiresAtFromToken(token)
            .toInstant()
            .atOffset(ZoneOffset.UTC)

        val refreshTokenEntity = RefreshTokenEntity(
            account = account,
            tokenHash = toHashToken(token),
            isActive = true,
            expiresAt = expiresAt
        )

        refreshTokenRepository.save(refreshTokenEntity)

        return token
    }

    override fun generateAccessToken(account: AccountEntity): String {
        val userId: UUID = requireNotNull(account.id) { ACCOUNT_ID_MUST_BE_NOT_NULL }
        val token = jwtProvider.generateAccessToken(userId)

        return token
    }

    @Transactional
    override fun refreshTokens(refreshToken: String): TokenResponseDto {
        val hashedToken = toHashToken(refreshToken)

        val refreshTokenEntity: RefreshTokenEntity = refreshTokenRepository.findByTokenHash(hashedToken)
            ?: throw TokenNotFoundException("Token is not exists")

        if (!jwtProvider.validateToken(refreshToken)) {
            throw InvalidTokenException("Token is invalid")
        }

        if (!refreshTokenEntity.isActive!!) {
            val account = refreshTokenEntity.account
            invalidateAllAccountTokens(account!!)

            throw InvalidTokenException("Token is already used")
        }

        refreshTokenEntity.isActive = false
        refreshTokenRepository.save(refreshTokenEntity)

        val account = refreshTokenEntity.account!!
        val newAccessToken = generateAccessToken(account)
        val newRefreshToken = generateRefreshToken(account)

        return TokenResponseDto(newAccessToken, newRefreshToken)
    }

    private fun invalidateAllAccountTokens(account: AccountEntity) {
        val accountId: UUID = requireNotNull(account.id) { ACCOUNT_ID_MUST_BE_NOT_NULL}

        refreshTokenRepository.deactivateAllByAccountId(accountId)
    }

    private fun toHashToken(token: String): String {
        val bytes = MessageDigest.getInstance(HASH_ALGORITHM).digest(token.toByteArray(Charsets.UTF_8))

        return HexFormat.of().formatHex(bytes)
    }

}