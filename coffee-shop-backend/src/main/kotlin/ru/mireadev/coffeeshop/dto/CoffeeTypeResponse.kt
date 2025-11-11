package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Тип кофейного напитка")
data class CoffeeTypeResponse(
    @field:Schema(description = "Id типа кофе", example = "1")
    val id : Int,
    
    @field:Schema(description = "Название типа кофе", example = "На молоке")
    val type : String
)