package ru.mireadev.coffeeshop.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Информация о кофе в корзине")
data class CoffeeCartResponse(
    @field:Schema(description = "Id кофе", example = "2")
    val id: Int,

    @field:Schema(description = "Название кофе", example = "Раф")
    val name: String,

    @field:Schema(description = "Выбранный размер", example = "L")
    val selectedSize: String,

    @field:Schema(description = "Цена за единицу", example = "345.9")
    val price: Float,

    @field:Schema(description = "Количество", example = "2")
    val quantity: Int,

    @field:Schema(description = "Общая стоимость", example = "691.8")
    val totalPrice: Float,

    @field:Schema(description = "Ссылка на изображение", example = "raf.jpg")
    val imageName: String
)

@Schema(description = "Общая информация о корзине")
data class CartSummaryResponse(
    @field:Schema(description = "Товары в корзине")
    val items: List<CoffeeCartResponse>,

    @field:Schema(description = "Общее количество товаров", example = "5")
    val totalItems: Int,

    @field:Schema(description = "Общая стоимость корзины", example = "1250.5")
    val totalPrice: Float
)