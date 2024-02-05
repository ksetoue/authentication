package io.ksetoue.login.domain

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "customer")
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    val name: String?,
    val serviceRendered: String,
    val address: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)