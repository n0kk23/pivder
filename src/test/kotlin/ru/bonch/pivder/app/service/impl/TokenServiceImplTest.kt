package ru.bonch.pivder.app.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import ru.bonch.pivder.app.entity.AccountEntity
import ru.bonch.pivder.app.entity.RefreshTokenEntity
import ru.bonch.pivder.app.exception.notfound.impl.TokenNotFoundException
import ru.bonch.pivder.app.exception.unauthorized.impl.InvalidTokenException
import ru.bonch.pivder.app.jwt.JwtProvider
import ru.bonch.pivder.app.repository.RefreshTokenRepository
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.*

class TokenServiceImplTest {
    private lateinit var tokenService: TokenServiceImpl
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var jwtProvider: JwtProvider

    private val userId = UUID.randomUUID()
    private val accessToken = "header.payload.access"
    private val refreshToken = "header.payload.refresh"

    private fun buildAccount() = AccountEntity(
        id = userId,
        username = "john",
        hashPassword = "hashed",
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )

    private fun buildRefreshTokenEntity(
        account: AccountEntity,
        tokenHash: String,
        isActive: Boolean = true
    ) = RefreshTokenEntity(
        account = account,
        tokenHash = tokenHash,
        isActive = isActive,
        expiresAt = OffsetDateTime.now().plusDays(7)
    )

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)
        jwtProvider = mock(JwtProvider::class.java)
        tokenService = TokenServiceImpl(refreshTokenRepository, jwtProvider)
    }

    @Test
    fun `generateAccessToken should return token from jwtProvider`() {
        // given
        val account = buildAccount()
        `when`(jwtProvider.generateAccessToken(userId)).thenReturn(accessToken)

        // when
        val result = tokenService.generateAccessToken(account)

        // then
        assertEquals(accessToken, result)
    }

    @Test
    fun `generateAccessToken should throw when account id is null`() {
        // given
        val account = buildAccount().apply { id = null }

        // when & then
        assertThrows<IllegalArgumentException> { tokenService.generateAccessToken(account) }
    }
    @Test
    fun `generateRefreshToken should return token from jwtProvider`() {
        // given
        val account = buildAccount()
        `when`(jwtProvider.generateRefreshToken(userId)).thenReturn(refreshToken)
        `when`(jwtProvider.extractExpiresAtFromToken(refreshToken)).thenReturn(Date())

        // when
        val result = tokenService.generateRefreshToken(account)

        // then
        assertEquals(refreshToken, result)
    }

    @Test
    fun `generateRefreshToken should save RefreshTokenEntity to repository`() {
        // given
        val account = buildAccount()
        `when`(jwtProvider.generateRefreshToken(userId)).thenReturn(refreshToken)
        `when`(jwtProvider.extractExpiresAtFromToken(refreshToken)).thenReturn(Date())

        // when
        tokenService.generateRefreshToken(account)

        // then
        verify(refreshTokenRepository).save(any(RefreshTokenEntity::class.java))
    }

    @Test
    fun `generateRefreshToken should throw when account id is null`() {
        // given
        val account = buildAccount().apply { id = null }

        // when & then
        assertThrows<IllegalArgumentException> { tokenService.generateRefreshToken(account) }
    }

    @Test
    fun `refreshTokens should return new TokenResponseDto on success`() {
        // given
        val account = buildAccount()
        val hashedToken = hashToken()
        val entity = buildRefreshTokenEntity(account, hashedToken, isActive = true)

        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(entity)
        `when`(jwtProvider.validateToken(refreshToken)).thenReturn(true)
        `when`(jwtProvider.generateAccessToken(userId)).thenReturn(accessToken)
        `when`(jwtProvider.generateRefreshToken(userId)).thenReturn("new.refresh.token")
        `when`(jwtProvider.extractExpiresAtFromToken("new.refresh.token")).thenReturn(Date())

        // when
        val result = tokenService.refreshTokens(refreshToken)

        // then
        assertEquals(accessToken, result.accessToken)
        assertEquals("new.refresh.token", result.refreshToken)
    }

    @Test
    fun `refreshTokens should throw TokenNotFoundException when token not found`() {
        // given
        val hashedToken = hashToken()
        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(null)

        // when & then
        assertThrows<TokenNotFoundException> { tokenService.refreshTokens(refreshToken) }
    }

    @Test
    fun `refreshTokens should throw InvalidTokenException when token is not valid`() {
        // given
        val account = buildAccount()
        val hashedToken = hashToken()
        val entity = buildRefreshTokenEntity(account, hashedToken, isActive = true)

        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(entity)
        `when`(jwtProvider.validateToken(refreshToken)).thenReturn(false)

        // when & then
        assertThrows<InvalidTokenException> { tokenService.refreshTokens(refreshToken) }
    }

    @Test
    fun `refreshTokens should throw InvalidTokenException when token is already used`() {
        // given
        val account = buildAccount()
        val hashedToken = hashToken()
        val entity = buildRefreshTokenEntity(account, hashedToken, isActive = false)

        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(entity)
        `when`(jwtProvider.validateToken(refreshToken)).thenReturn(true)

        // when & then
        assertThrows<InvalidTokenException> { tokenService.refreshTokens(refreshToken) }
    }

    @Test
    fun `refreshTokens should invalidate all account tokens when token is already used`() {
        // given
        val account = buildAccount()
        val hashedToken = hashToken()
        val entity = buildRefreshTokenEntity(account, hashedToken, isActive = false)

        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(entity)
        `when`(jwtProvider.validateToken(refreshToken)).thenReturn(true)

        // when
        runCatching { tokenService.refreshTokens(refreshToken) }

        // then
        verify(refreshTokenRepository).deactivateAllByAccountId(userId)
    }

    @Test
    fun `refreshTokens should deactivate used refresh token`() {
        // given
        val account = buildAccount()
        val hashedToken = hashToken()
        val entity = buildRefreshTokenEntity(account, hashedToken, isActive = true)

        `when`(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(entity)
        `when`(jwtProvider.validateToken(refreshToken)).thenReturn(true)
        `when`(jwtProvider.generateAccessToken(userId)).thenReturn(accessToken)
        `when`(jwtProvider.generateRefreshToken(userId)).thenReturn("new.refresh.token")
        `when`(jwtProvider.extractExpiresAtFromToken("new.refresh.token")).thenReturn(Date())

        // when
        tokenService.refreshTokens(refreshToken)

        // then
        verify(refreshTokenRepository).save(argThat { it: RefreshTokenEntity -> !it.isActive!! })
    }

    private fun hashToken(): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(refreshToken.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(bytes)
    }
}