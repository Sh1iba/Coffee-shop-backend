package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal,

    @Column(name = "delivery_fee", nullable = false)
    var deliveryFee: BigDecimal = BigDecimal.ZERO,

    @Column(name = "delivery_address", nullable = false)
    val deliveryAddress: String,

    @Column(name = "order_date", nullable = false)
    val orderDate: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @Column(name = "coffee_name", nullable = false)
    val coffeeName: String,

    @Column(name = "selected_size", nullable = false)
    val selectedSize: String,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: BigDecimal,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "total_price", nullable = false)
    val totalPrice: BigDecimal
)