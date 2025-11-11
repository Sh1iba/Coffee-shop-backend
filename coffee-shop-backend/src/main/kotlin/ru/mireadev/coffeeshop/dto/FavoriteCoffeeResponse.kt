package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Информация о кофе в избранном")
data class FavoriteCoffeeResponse(
    @field:Schema(description = "Id кофе", example = "2")
    val id: Int
) 