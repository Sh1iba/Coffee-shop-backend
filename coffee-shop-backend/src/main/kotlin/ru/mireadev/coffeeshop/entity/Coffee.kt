package ru.mireadev.coffeeshop.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Entity
@Table(name = "coffee")
data class Coffee @JvmOverloads constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int = 0,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    var type: CoffeeType = CoffeeType(),

    @Size(max = 20)
    @NotNull
    @Column(name = "name", nullable = false, length = 20)
    var name: String = "",

    @NotNull
    @Column(name = "price", nullable = false, precision = 5, scale = 2)
    var price: BigDecimal = 0.toBigDecimal(),

    @Size(max = 500)
    @NotNull
    @Column(name = "description", nullable = false, length = 500)
    var description: String = "",

    @Size(max = 50)
    @NotNull
    @Column(name = "image_name", nullable = false, length = 50)
    var imageName: String = ""
)