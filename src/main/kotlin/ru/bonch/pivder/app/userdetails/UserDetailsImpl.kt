package ru.bonch.pivder.app.userdetails

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.bonch.pivder.app.entity.AccountEntity

class UserDetailsImpl(
    private val account: AccountEntity
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return emptyList()
    }

    override fun getPassword(): String {
        return account.hashPassword
    }

    override fun getUsername(): String {
        return account.username
    }

}