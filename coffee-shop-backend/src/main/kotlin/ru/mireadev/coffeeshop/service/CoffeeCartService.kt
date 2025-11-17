package ru.mireadev.coffeeshop.service

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.mireadev.coffeeshop.dto.CartSummaryResponse
import ru.mireadev.coffeeshop.dto.CoffeeCartRequest
import ru.mireadev.coffeeshop.dto.CoffeeCartResponse
import ru.mireadev.coffeeshop.dto.UpdateCartQuantityRequest
import ru.mireadev.coffeeshop.entity.CoffeeCart
import ru.mireadev.coffeeshop.repository.CoffeeCartRepository
import ru.mireadev.coffeeshop.repository.CoffeeRepository
import ru.mireadev.coffeeshop.repository.CoffeeSizeRepository
import ru.mireadev.coffeeshop.repository.UserRepository

@Service
class CoffeeCartService(
    private val coffeeCartRepository: CoffeeCartRepository,
    private val coffeeRepository: CoffeeRepository,
    private val userRepository: UserRepository,
    private val coffeeSizeRepository: CoffeeSizeRepository
) {

    fun getCart(userId: Long): ResponseEntity<CartSummaryResponse> {
        val cartItems = coffeeCartRepository.findAllByUserId(userId)

        val cartResponses = cartItems.mapNotNull { cartItem ->
            val coffee = cartItem.coffee ?: return@mapNotNull null
            val price = getCoffeePrice(coffee.id, cartItem.selectedSize)

            CoffeeCartResponse(
                id = coffee.id,
                name = coffee.name,
                selectedSize = cartItem.selectedSize,
                price = price,
                quantity = cartItem.quantity,
                totalPrice = price * cartItem.quantity,
                imageName = coffee.imageName
            )
        }

        val totalItems = cartResponses.sumOf { it.quantity }
        val totalPrice = cartResponses.sumOf { it.totalPrice.toDouble() }.toFloat()

        val summary = CartSummaryResponse(
            items = cartResponses,
            totalItems = totalItems,
            totalPrice = totalPrice
        )

        return ResponseEntity.ok(summary)
    }

    @Transactional
    fun addToCart(userId: Long, request: CoffeeCartRequest): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "User not found"))

        val coffee = coffeeRepository.findById(request.coffeeId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "Coffee not found"))

        // Проверяем валидность размера
        if (!isValidSize(request.coffeeId, request.selectedSize)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Invalid size for this coffee"))
        }

        // Проверяем, есть ли уже такой товар в корзине
        val existingItem = coffeeCartRepository.findByUserIdAndCoffeeIdAndSelectedSize(
            userId, request.coffeeId, request.selectedSize
        )

        return if (existingItem != null) {
            // Обновляем количество, если товар уже есть в корзине
            existingItem.quantity += request.quantity
            coffeeCartRepository.save(existingItem)
            ResponseEntity.status(HttpStatus.OK)
                .body(mapOf("message" to "Cart item quantity updated"))
        } else {
            // Добавляем новый товар в корзину
            val cartItem = CoffeeCart(
                userId = userId,
                coffeeId = request.coffeeId,
                selectedSize = request.selectedSize,
                quantity = request.quantity
            )
            coffeeCartRepository.save(cartItem)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("message" to "Coffee added to cart"))
        }
    }

    @Transactional
    fun updateQuantity(userId: Long, coffeeId: Int, selectedSize: String, request: UpdateCartQuantityRequest): ResponseEntity<Any> {
        if (request.quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Quantity must be greater than 0"))
        }

        val cartItem = coffeeCartRepository.findByUserIdAndCoffeeIdAndSelectedSize(userId, coffeeId, selectedSize)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Cart item not found"))

        cartItem.quantity = request.quantity
        coffeeCartRepository.save(cartItem)

        return ResponseEntity.ok(mapOf("message" to "Cart item quantity updated"))
    }

    @Transactional
    fun removeFromCart(userId: Long, coffeeId: Int, selectedSize: String): ResponseEntity<Any> {
        if (!coffeeCartRepository.existsByUserIdAndCoffeeIdAndSelectedSize(userId, coffeeId, selectedSize)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Cart item not found"))
        }

        coffeeCartRepository.deleteByUserIdAndCoffeeIdAndSelectedSize(userId, coffeeId, selectedSize)
        return ResponseEntity.ok(mapOf("message" to "Cart item removed"))
    }

    @Transactional
    fun clearCart(userId: Long): ResponseEntity<Any> {
        coffeeCartRepository.deleteAllByUserId(userId)
        return ResponseEntity.ok(mapOf("message" to "Cart cleared"))
    }

    private fun isValidSize(coffeeId: Int, size: String): Boolean {
        val sizes = coffeeSizeRepository.findAllByCoffeeId(coffeeId)
        return sizes.any { it.size == size }
    }

    private fun getCoffeePrice(coffeeId: Int, size: String): Float {
        val coffeeSize = coffeeSizeRepository.findAllByCoffeeId(coffeeId)
            .find { it.size == size }
        return coffeeSize?.price?.toFloat() ?: 0f
    }
}