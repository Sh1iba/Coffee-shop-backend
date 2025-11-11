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
import ru.mireadev.coffeeshop.service.CoffeeService
import ru.mireadev.coffeeshop.service.FavoriteCoffeeService
import ru.mireadev.coffeeshop.service.ImageStorageService
import ru.mireadev.coffeeshop.service.UserService


@RestController
@RequestMapping("/api/coffee")
@Tag(name = "Кофе", description = "Эндпоинты для работы с кофе и избранным")
class CoffeeController(
    private val coffeeService: CoffeeService,
    private val imageStorageService: ImageStorageService,
    private val favoriteCoffeeService: FavoriteCoffeeService,
    private val userService: UserService
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
        description = "Удаляет выбранный кофейный напиток из избранного пользователя",
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
        @PathVariable coffeeId: Int
    ): ResponseEntity<Any> {
        val userId = userService.getUserIdFromAuthentication(authentication)
        return favoriteCoffeeService.removeFromFavorites(userId, coffeeId)
    }
}