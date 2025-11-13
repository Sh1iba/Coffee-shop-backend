package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос на добавление кофе в избранное")
data class FavoriteCoffeeRequest(
    @field:Schema(description = "Id кофе", example = "1", nullable = false, required = true)
    val coffeeId: Int,

    @field:Schema(description = "Выбранный размер", example = "L", nullable = false, required = true)
    val selectedSize: String
)