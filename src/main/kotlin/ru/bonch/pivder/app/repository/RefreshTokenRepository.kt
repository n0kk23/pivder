package ru.bonch.pivder.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.bonch.pivder.app.entity.RefreshTokenEntity
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {

    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    @Modifying
    @Query(
        """
            UPDATE refresh_tokens
            SET is_active = false
            WHERE account_id = :accountId AND is_active = true
        """, nativeQuery = true)
    fun deactivateAllByAccountId(@Param("accountId") accountId: UUID)
}