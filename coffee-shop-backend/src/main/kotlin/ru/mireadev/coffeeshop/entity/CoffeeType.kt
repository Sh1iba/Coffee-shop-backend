package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(name = "coffee_type")
data class CoffeeType @JvmOverloads constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int = 0,

    @Size(max = 9)
    @NotNull
    @Column(name = "type", nullable = false, length = 9)
    var type: String = ""
)