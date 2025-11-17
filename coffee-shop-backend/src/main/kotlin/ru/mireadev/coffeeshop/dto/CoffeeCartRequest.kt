package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос на добавление кофе в корзину")
data class CoffeeCartRequest(
    @field:Schema(description = "Id кофе", example = "1", required = true)
    val coffeeId: Int,

    @field:Schema(description = "Выбранный размер", example = "L", required = true)
    val selectedSize: String,

    @field:Schema(description = "Количество", example = "2", defaultValue = "1")
    val quantity: Int = 1
)

@Schema(description = "Запрос на обновление количества кофе в корзине")
data class UpdateCartQuantityRequest(
    @field:Schema(description = "Новое количество", example = "3", required = true)
    val quantity: Int
)