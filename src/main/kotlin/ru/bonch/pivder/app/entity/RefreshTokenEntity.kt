package ru.bonch.pivder.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    var account: AccountEntity?,

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String?,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean?,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: OffsetDateTime?,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null
)