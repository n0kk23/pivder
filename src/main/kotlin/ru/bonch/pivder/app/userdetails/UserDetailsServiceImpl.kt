package ru.bonch.pivder.app.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.bonch.pivder.app.repository.AccountRepository
import java.util.*

@Service
class UserDetailsServiceImpl(
    private val accountRepository: AccountRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails = loadByUserId(UUID.fromString(username))

    fun loadByUserId(userId: UUID): UserDetails =
        accountRepository.findById(userId)
            .map { UserDetailsImpl(it) }
            .orElseThrow { UsernameNotFoundException("User not found with id: $userId") }
}