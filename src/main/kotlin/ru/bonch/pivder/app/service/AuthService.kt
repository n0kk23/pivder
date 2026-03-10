package ru.bonch.pivder.app.service

import ru.bonch.pivder.app.command.AccountAuthorizationCommand
import ru.bonch.pivder.app.command.AccountRegistrationCommand
import ru.bonch.pivder.app.dto.response.AccountResponseDto
import ru.bonch.pivder.app.dto.response.TokenResponseDto

interface AuthService {
    fun registration(command: AccountRegistrationCommand): AccountResponseDto
    fun authorization(command: AccountAuthorizationCommand): TokenResponseDto
}