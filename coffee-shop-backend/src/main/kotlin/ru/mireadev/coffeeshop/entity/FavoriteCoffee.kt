package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*

@Entity
@Table(name = "favorite_coffee")
@IdClass(FavoriteCoffeeId::class)
data class FavoriteCoffee(
    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Id
    @Column(name = "coffee_id")
    val coffeeId: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id", insertable = false, updatable = false)
    val coffee: Coffee? = null
)

data class FavoriteCoffeeId(
    val userId: Long = 0,
    val coffeeId: Int = 0
) : java.io.Serializable 