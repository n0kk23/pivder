package ru.bonch.pivder.app.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.bonch.pivder.app.userdetails.UserDetailsServiceImpl

@Component
class JwtFilter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: UserDetailsServiceImpl
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && jwtProvider.validateToken(token)) {
            val userId = jwtProvider.extractUserIdFromToken(token)
            val principal = userDetailsService.loadByUserId(userId)

            val auth = UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities
            )

            SecurityContextHolder.getContext().authentication = auth
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
            ?: return null

        if (!header.startsWith("Bearer ")) {
            return null
        }

        return header.substring(7)
    }

}