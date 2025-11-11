package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Кофейный напиток")
data class CoffeeResponse (
    @field:Schema(description = "Id кофе", example = "2")
    val id: Int,

    @field:Schema(description = "Тип кофе", example = "На молоке")
    val type: CoffeeTypeResponse,

    @field:Schema(description = "Название кофе", example = "Раф")
    val name: String,

    @field:Schema(description = "Опсиание кофе", example = "Раф - это кофейный напиток, подобный латте, но со сливками, более обильной молочной пеной и ароматными сиропами.")
    val description: String,

    @field:Schema(description = "Цена", example = "299,99")
    val price: Float,

    @field:Schema(description = "Ссылка на картинку", example = "хз пока")
    val imageName: String,
)