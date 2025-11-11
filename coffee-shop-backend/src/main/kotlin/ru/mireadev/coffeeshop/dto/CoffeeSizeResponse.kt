package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Размер и цена кофе")
data class CoffeeSizeResponse (

    @field:Schema(description = "Размер кофе", example = "S")
    val size: String,

    @field:Schema(description = "Цена кофе", example = "235.9")
    val price: Float
)