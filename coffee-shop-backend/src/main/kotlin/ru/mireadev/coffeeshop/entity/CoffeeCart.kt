package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*

@Entity
@Table(name = "coffee_cart")
@IdClass(CoffeeCartId::class)
data class CoffeeCart(
    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Id
    @Column(name = "coffee_id")
    val coffeeId: Int,

    @Id
    @Column(name = "selected_size", nullable = false, length = 10)
    val selectedSize: String,

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id", insertable = false, updatable = false)
    val coffee: Coffee? = null
)

data class CoffeeCartId(
    val userId: Long = 0,
    val coffeeId: Int = 0,
    val selectedSize: String = ""
) : java.io.Serializable