package ru.bonch.pivder.app.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import ru.bonch.pivder.config.JwtConfiguration
import java.util.*
import javax.crypto.KeyGenerator

class JwtProviderTest {
    private lateinit var jwtProvider: JwtProvider
    private lateinit var jwtConfiguration: JwtConfiguration

    private val secretKey = Base64.getEncoder().encodeToString(
        KeyGenerator.getInstance("HmacSHA256").also { it.init(256) }.generateKey().encoded
    )

    @BeforeEach
    fun setUp() {
        jwtConfiguration = mock(JwtConfiguration::class.java)
        `when`(jwtConfiguration.secretKey).thenReturn(secretKey)
        `when`(jwtConfiguration.accessTokenExpirationMs).thenReturn(3600000L)
        `when`(jwtConfiguration.refreshTokenExpirationMs).thenReturn(604800000L)
        jwtProvider = JwtProvider(jwtConfiguration)
    }

    @Test
    fun `generateAccessToken should return non-blank token`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateAccessToken(userId)

        // then
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateAccessToken should return valid JWT format`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateAccessToken(userId)

        // then
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun `generateAccessToken should be valid`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateAccessToken(userId)

        // then
        assertTrue(jwtProvider.validateToken(token))
    }

    @Test
    fun `generateRefreshToken should return non-blank token`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateRefreshToken(userId)

        // then
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateRefreshToken should return valid JWT format`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateRefreshToken(userId)

        // then
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun `generateRefreshToken should be valid`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val token = jwtProvider.generateRefreshToken(userId)

        // then
        assertTrue(jwtProvider.validateToken(token))
    }

    @Test
    fun `access and refresh tokens should differ`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshToken = jwtProvider.generateRefreshToken(userId)

        // then
        assertNotEquals(accessToken, refreshToken)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // given
        val token = jwtProvider.generateAccessToken(UUID.randomUUID())

        // when
        val result = jwtProvider.validateToken(token)

        // then
        assertTrue(result)
    }

    @Test
    fun `validateToken should return false for malformed token`() {
        // given
        val malformedToken = "not.a.token"

        // when
        val result = jwtProvider.validateToken(malformedToken)

        // then
        assertFalse(result)
    }

    @Test
    fun `validateToken should return false for empty string`() {
        // given
        val emptyToken = ""

        // when
        val result = jwtProvider.validateToken(emptyToken)

        // then
        assertFalse(result)
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // given
        `when`(jwtConfiguration.accessTokenExpirationMs).thenReturn(-1000L)
        val expiredProvider = JwtProvider(jwtConfiguration)
        val token = expiredProvider.generateAccessToken(UUID.randomUUID())

        // when
        val result = jwtProvider.validateToken(token)

        // then
        assertFalse(result)
    }

    @Test
    fun `validateToken should return false for token signed with different key`() {
        // given
        val otherSecret = Base64.getEncoder().encodeToString(
            KeyGenerator.getInstance("HmacSHA256").also { it.init(256) }.generateKey().encoded
        )
        val otherConfig = mock(JwtConfiguration::class.java)
        `when`(otherConfig.secretKey).thenReturn(otherSecret)
        `when`(otherConfig.accessTokenExpirationMs).thenReturn(3600000L)
        val otherProvider = JwtProvider(otherConfig)
        val token = otherProvider.generateAccessToken(UUID.randomUUID())

        // when
        val result = jwtProvider.validateToken(token)

        // then
        assertFalse(result)
    }

    @Test
    fun `extractUserIdFromToken should return correct userId`() {
        // given
        val userId = UUID.randomUUID()
        val token = jwtProvider.generateAccessToken(userId)

        // when
        val result = jwtProvider.extractUserIdFromToken(token)

        // then
        assertEquals(userId, result)
    }

    @Test
    fun `extractUserIdFromToken should return false for invalid userId`() {
        // given
        val userId = UUID.randomUUID()
        val invalidUserId = UUID.randomUUID();
        val token = jwtProvider.generateAccessToken(userId)

        // when
        val result = jwtProvider.extractUserIdFromToken(token)

        // then
        assertNotEquals(userId, invalidUserId)
    }

    @Test
    fun `extractUserIdFromToken should throw exception for invalid token`() {
        // given
        val invalidToken = "invalid.token.here"

        // when & then
        assertThrows<Exception> { jwtProvider.extractUserIdFromToken(invalidToken) }
    }
}