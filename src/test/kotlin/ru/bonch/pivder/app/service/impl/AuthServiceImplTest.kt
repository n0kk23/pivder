package ru.bonch.pivder.app.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder
import ru.bonch.pivder.app.command.AccountAuthorizationCommand
import ru.bonch.pivder.app.command.AccountRegistrationCommand
import ru.bonch.pivder.app.dto.response.AccountResponseDto
import ru.bonch.pivder.app.dto.response.TokenResponseDto
import ru.bonch.pivder.app.entity.AccountEntity
import ru.bonch.pivder.app.exception.conflict.impl.UsernameIsAlreadyTakenException
import ru.bonch.pivder.app.exception.unauthorized.impl.BadCredentialsException
import ru.bonch.pivder.app.mapper.AccountMapper
import ru.bonch.pivder.app.repository.AccountRepository
import ru.bonch.pivder.app.service.TokenService
import java.time.OffsetDateTime
import java.util.*

class AuthServiceImplTest {
    private lateinit var authService: AuthServiceImpl
    private lateinit var accountRepository: AccountRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var accountMapper: AccountMapper
    private lateinit var tokenService: TokenService

    private fun buildAccount(
        username: String = "john",
        hashPassword: String = "hashed"
    ) = AccountEntity(
        id = UUID.randomUUID(),
        username = username,
        hashPassword = hashPassword,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )

    private fun buildResponseDto(
        username: String = "john"
    ) = AccountResponseDto(
        id = UUID.randomUUID(),
        username = username,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        accountRepository = mock(AccountRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        accountMapper = mock(AccountMapper::class.java)
        tokenService = mock(TokenService::class.java)
        authService = AuthServiceImpl(accountRepository, passwordEncoder, accountMapper, tokenService)
    }

    @Test
    fun `registration should return AccountResponseDto on success`() {
        // given
        val command = AccountRegistrationCommand(username = "john", password = "secret")
        val account = buildAccount()
        val responseDto = buildResponseDto()

        `when`(accountRepository.existsByUsername("john")).thenReturn(false)
        `when`(passwordEncoder.encode("secret")).thenReturn("hashed")
        `when`(accountRepository.save(any(AccountEntity::class.java))).thenReturn(account)
        `when`(accountMapper.toResponse(account)).thenReturn(responseDto)

        // when
        val result = authService.registration(command)

        // then
        assertEquals(responseDto, result)
    }

    @Test
    fun `registration should throw UsernameIsAlreadyTakenException when username is taken`() {
        // given
        val command = AccountRegistrationCommand(username = "john", password = "secret")
        `when`(accountRepository.existsByUsername("john")).thenReturn(true)

        // when & then
        assertThrows<UsernameIsAlreadyTakenException> { authService.registration(command) }
    }

    @Test
    fun `registration should encode password before saving`() {
        // given
        val command = AccountRegistrationCommand(username = "jane", password = "pass123")
        val account = buildAccount(username = "jane")
        val responseDto = buildResponseDto(username = "jane")

        `when`(accountRepository.existsByUsername("jane")).thenReturn(false)
        `when`(passwordEncoder.encode("pass123")).thenReturn("encoded")
        `when`(accountRepository.save(any(AccountEntity::class.java))).thenReturn(account)
        `when`(accountMapper.toResponse(account)).thenReturn(responseDto)

        // when
        authService.registration(command)

        // then
        verify(passwordEncoder).encode("pass123")
    }

    @Test
    fun `registration should save account to repository`() {
        // given
        val command = AccountRegistrationCommand(username = "jane", password = "pass123")
        val account = buildAccount(username = "jane")
        val responseDto = buildResponseDto(username = "jane")

        `when`(accountRepository.existsByUsername("jane")).thenReturn(false)
        `when`(passwordEncoder.encode("pass123")).thenReturn("encoded")
        `when`(accountRepository.save(any(AccountEntity::class.java))).thenReturn(account)
        `when`(accountMapper.toResponse(account)).thenReturn(responseDto)

        // when
        authService.registration(command)

        // then
        verify(accountRepository).save(any(AccountEntity::class.java))
    }

    @Test
    fun `authorization should return TokenResponseDto on success`() {
        // given
        val command = AccountAuthorizationCommand(username = "john", password = "secret")
        val account = buildAccount()

        `when`(accountRepository.findByUsername("john")).thenReturn(account)
        `when`(passwordEncoder.matches("secret", "hashed")).thenReturn(true)
        `when`(tokenService.generateAccessToken(account)).thenReturn("access-token")
        `when`(tokenService.generateRefreshToken(account)).thenReturn("refresh-token")

        // when
        val result = authService.authorization(command)

        // then
        assertEquals(TokenResponseDto("access-token", "refresh-token"), result)
    }

    @Test
    fun `authorization should throw BadCredentialsException when user not found`() {
        // given
        val command = AccountAuthorizationCommand(username = "ghost", password = "secret")
        `when`(accountRepository.findByUsername("ghost")).thenReturn(null)

        // when & then
        assertThrows<BadCredentialsException> { authService.authorization(command) }
    }

    @Test
    fun `authorization should throw BadCredentialsException when password is wrong`() {
        // given
        val command = AccountAuthorizationCommand(username = "john", password = "wrong")
        val account = buildAccount()

        `when`(accountRepository.findByUsername("john")).thenReturn(account)
        `when`(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        // when & then
        assertThrows<BadCredentialsException> { authService.authorization(command) }
    }

    @Test
    fun `authorization should generate both access and refresh tokens`() {
        // given
        val command = AccountAuthorizationCommand(username = "john", password = "secret")
        val account = buildAccount()

        `when`(accountRepository.findByUsername("john")).thenReturn(account)
        `when`(passwordEncoder.matches("secret", "hashed")).thenReturn(true)
        `when`(tokenService.generateAccessToken(account)).thenReturn("access-token")
        `when`(tokenService.generateRefreshToken(account)).thenReturn("refresh-token")

        // when
        authService.authorization(command)

        // then
        verify(tokenService).generateAccessToken(account)
        verify(tokenService).generateRefreshToken(account)
    }
}