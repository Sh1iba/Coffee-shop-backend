package ru.mireadev.coffeeshop.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.mireadev.coffeeshop.dto.*
import ru.mireadev.coffeeshop.service.*


@RestController
@RequestMapping("/api/coffee")
@Tag(name = "Кофе", description = "Эндпоинты для работы с кофе, избранным и корзиной")
class CoffeeController(
    private val coffeeService: CoffeeService,
    private val imageStorageService: ImageStorageService,
    private val favoriteCoffeeService: FavoriteCoffeeService,
    private val coffeeCartService: CoffeeCartService,
    private val userService: UserService,
    private val orderService: OrderService
) {

    @Operation(
        summary = "Получение всех типов кофе",
        description = "Возвращает список всех доступных типов кофе",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список типов кофе успешно получен",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = CoffeeTypeResponse::class))
                )]
            )
        ]
    )
    @GetMapping("/types")
    fun getAllCoffeeTypes(): ResponseEntity<List<CoffeeTypeResponse>> {
        return coffeeService.getAllCoffeeType()
    }

    @Operation(
        summary = "Получение всех кофейных напитков",
        description = "Возвращает список всех доступных кофейных напитков",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список кофе успешно получен",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = CoffeeResponse::class))
                )]
            )
        ]
    )
    @GetMapping()
    fun getAllCoffee(): ResponseEntity<List<CoffeeResponse>>{
        return coffeeService.getAllCoffee()
    }

    @Operation(
        summary = "Получение изображения кофе",
        description = "Возвращает изображение кофе по имени файла",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Изображение успешно получено",
                content = [Content(mediaType = "image/*")]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Изображение не найдено"
            )
        ]
    )
    @GetMapping("/image/{imageName}")
    fun getImageCoffeeByImageName(
        @Parameter(description = "Имя файла изображения", required = true)
        @PathVariable("imageName") imageName: String
    ): ResponseEntity<Resource>{
        val resource = imageStorageService.getImageResource(imageName)

        // Определяем Content-Type по расширению файла
        val contentType = when (imageName.substringAfterLast('.').lowercase()) {
            "jpg", "jpeg" -> MediaType.IMAGE_JPEG
            "png" -> MediaType.IMAGE_PNG
            "gif" -> MediaType.IMAGE_GIF
            else -> MediaType.APPLICATION_OCTET_STREAM
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            .body(resource)
    }

    @Operation(
        summary = "Получение избранных кофейных напитков",
        description = "Возвращает список кофейных напитков, добавленных пользователем в избранное",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список избранного успешно получен",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = FavoriteCoffeeResponse::class))
                )]
            )
        ]
    )
    @GetMapping("/favorites")
    fun getFavorites(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication
    ): ResponseEntity<List<FavoriteCoffeeResponse>> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return favoriteCoffeeService.getFavorites(userId)
    }

    @Operation(
        summary = "Добавление кофе в избранное",
        description = "Добавляет выбранный кофейный напиток в избранное пользователя",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Кофе успешно добавлен в избранное",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Кофе или пользователь не найден",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Кофе уже добавлен в избранное",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/favorites")
    fun addToFavorites(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "Данные о кофе для добавления в избранное", required = true)
        @RequestBody request: FavoriteCoffeeRequest
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return favoriteCoffeeService.addToFavorites(userId, request)
    }

    @Operation(
        summary = "Удаление кофе из избранного",
        description = "Удаляет выбранный кофейный напиток из избранного пользователя. Можно удалить конкретный размер или все размеры",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Кофе успешно удален из избранного",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Кофе не найден в избранном",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @DeleteMapping("/favorites/{coffeeId}")
    fun removeFromFavorites(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "ID кофе для удаления из избранного", required = true)
        @PathVariable coffeeId: Int,
        @Parameter(description = "Конкретный размер для удаления (опционально)")
        @RequestParam(required = false) size: String?
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return favoriteCoffeeService.removeFromFavorites(userId, coffeeId, size)
    }

    @Operation(
        summary = "Получение содержимого корзины",
        description = "Возвращает полную информацию о корзине пользователя",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Корзина успешно получена",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CartSummaryResponse::class)
                )]
            )
        ]
    )
    @GetMapping("/cart")
    fun getCart(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication
    ): ResponseEntity<CartSummaryResponse> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return coffeeCartService.getCart(userId)
    }

    @Operation(
        summary = "Добавление кофе в корзину",
        description = "Добавляет кофейный напиток в корзину пользователя",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Кофе успешно добавлен в корзину",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "200",
                description = "Количество существующего товара обновлено",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Кофе или пользователь не найден",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Неверный размер для данного кофе",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/cart")
    fun addToCart(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "Данные о кофе для добавления в корзину", required = true)
        @RequestBody request: CoffeeCartRequest
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return coffeeCartService.addToCart(userId, request)
    }

    @Operation(
        summary = "Обновление количества товара в корзине",
        description = "Изменяет количество конкретного товара в корзине",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Количество успешно обновлено",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Товар не найден в корзине",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Количество должно быть больше 0",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @PutMapping("/cart/{coffeeId}/{selectedSize}")
    fun updateCartQuantity(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "ID кофе", required = true)
        @PathVariable coffeeId: Int,
        @Parameter(description = "Выбранный размер", required = true)
        @PathVariable selectedSize: String,
        @Parameter(description = "Новое количество", required = true)
        @RequestBody request: UpdateCartQuantityRequest
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return coffeeCartService.updateQuantity(userId, coffeeId, selectedSize, request)
    }

    @Operation(
        summary = "Удаление товара из корзины",
        description = "Удаляет конкретный товар из корзины пользователя",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Товар успешно удален из корзины",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Товар не найден в корзине",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @DeleteMapping("/cart/{coffeeId}/{selectedSize}")
    fun removeFromCart(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "ID кофе", required = true)
        @PathVariable coffeeId: Int,
        @Parameter(description = "Выбранный размер", required = true)
        @PathVariable selectedSize: String
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return coffeeCartService.removeFromCart(userId, coffeeId, selectedSize)
    }

    @Operation(
        summary = "Очистка корзины",
        description = "Полностью очищает корзину пользователя",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Корзина успешно очищена",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            )
        ]
    )
    @DeleteMapping("/cart")
    fun clearCart(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return coffeeCartService.clearCart(userId)
    }

    @Operation(
        summary = "Оформление заказа",
        description = "Создает заказ на основе выбранных товаров из корзины",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Заказ успешно создан",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Map::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Нет выбранных товаров или адрес не указан",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Map::class)
                )]
            )
        ]
    )
    @PostMapping("/checkout")
    fun checkout(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "Данные для оформления заказа", required = true)
        @RequestBody request: OrderRequest
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return orderService.createOrder(userId, request)
    }

    @Operation(
        summary = "Получение истории заказов",
        description = "Возвращает историю заказов пользователя",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "История заказов успешно получена",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = OrderResponse::class))
                )]
            )
        ]
    )
    @GetMapping("/orders/history")
    fun getOrderHistory(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return orderService.getOrderHistory(userId)
    }

    @Operation(
        summary = "Получение деталей заказа",
        description = "Возвращает детальную информацию о заказе",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Детали заказа успешно получены",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrderResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Заказ не найден",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Map::class)
                )]
            )
        ]
    )
    @GetMapping("/orders/{orderId}")
    fun getOrderDetails(
        @Parameter(description = "Данные аутентификации", hidden = true)
        authentication: Authentication,
        @Parameter(description = "ID заказа", required = true)
        @PathVariable orderId: Long
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return orderService.getOrderDetails(userId, orderId)
    }


}