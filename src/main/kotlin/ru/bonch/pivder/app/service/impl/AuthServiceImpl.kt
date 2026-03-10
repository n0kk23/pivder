package ru.bonch.pivder.app.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.bonch.pivder.app.command.AccountAuthorizationCommand
import ru.bonch.pivder.app.command.AccountRegistrationCommand
import ru.bonch.pivder.app.dto.response.AccountResponseDto
import ru.bonch.pivder.app.dto.response.TokenResponseDto
import ru.bonch.pivder.app.entity.AccountEntity
import ru.bonch.pivder.app.exception.conflict.impl.UsernameIsAlreadyTakenException
import ru.bonch.pivder.app.exception.unauthorized.impl.BadCredentialsException
import ru.bonch.pivder.app.mapper.AccountMapper
import ru.bonch.pivder.app.repository.AccountRepository
import ru.bonch.pivder.app.service.AuthService
import ru.bonch.pivder.app.service.TokenService

@Service
class AuthServiceImpl(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val accountMapper: AccountMapper,
    private val tokenService: TokenService,
) : AuthService {

    @Transactional
    override fun registration(command: AccountRegistrationCommand): AccountResponseDto {
        if (accountRepository.existsByUsername(command.username)) {
            throw UsernameIsAlreadyTakenException("Username '${command.username}' is already taken")
        }

        val account = AccountEntity(
            username = command.username,
            hashPassword = passwordEncoder.encode(command.password),
        )

        val savedAccount = accountRepository.save(account)

        return accountMapper.toResponse(savedAccount)
    }

    @Transactional
    override fun authorization(command: AccountAuthorizationCommand): TokenResponseDto {
        val account: AccountEntity = accountRepository.findByUsername(command.username)
            ?: throw BadCredentialsException("Wrong username or password")

        if (!passwordEncoder.matches(command.password, account.hashPassword)) {
            throw BadCredentialsException("Wrong username or password")
        }

        val accessToken = tokenService.generateAccessToken(account)
        val refreshToken = tokenService.generateRefreshToken(account)

        return TokenResponseDto(accessToken, refreshToken)
    }
}