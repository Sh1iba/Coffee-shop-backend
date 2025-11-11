package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*
import java.math.BigDecimal


@Entity
@Table(name = "coffee_size")
data class CoffeeSize(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id", nullable = false)
    val coffee: Coffee,

    @Column(name = "size", nullable = false)
    val size: String,

    @Column(name = "price", nullable = false)
    val price: BigDecimal

)