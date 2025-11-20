package ru.mireadev.coffeeshop.service

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.mireadev.coffeeshop.dto.CoffeeTypeResponse
import ru.mireadev.coffeeshop.dto.FavoriteCoffeeRequest
import ru.mireadev.coffeeshop.dto.FavoriteCoffeeResponse
import ru.mireadev.coffeeshop.entity.FavoriteCoffee
import ru.mireadev.coffeeshop.repository.CoffeeRepository
import ru.mireadev.coffeeshop.repository.FavoriteCoffeeRepository
import ru.mireadev.coffeeshop.repository.UserRepository

@Service
class FavoriteCoffeeService(
    private val favoriteCoffeeRepository: FavoriteCoffeeRepository,
    private val coffeeRepository: CoffeeRepository,
    private val userRepository: UserRepository
) {

    fun getFavorites(userId: Long): ResponseEntity<List<FavoriteCoffeeResponse>> {
        val favorites = favoriteCoffeeRepository.findAllByUserId(userId)

        val favoriteCoffeeResponses = favorites.mapNotNull { favoriteCoffee ->
            val coffee = favoriteCoffee.coffee ?: return@mapNotNull null

            FavoriteCoffeeResponse(
                id = coffee.id,
                selectedSize = favoriteCoffee.selectedSize
            )
        }

        return ResponseEntity.ok(favoriteCoffeeResponses)
    }

    fun addToFavorites(userId: Long, request: FavoriteCoffeeRequest): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "User not found"))

        val coffee = coffeeRepository.findById(request.coffeeId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "Coffee not found"))

        val validSizes = coffee.sizes.map { it.size }
        if (!validSizes.contains(request.selectedSize)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Invalid size for this coffee"))
        }

        if (favoriteCoffeeRepository.existsByUserIdAndCoffeeIdAndSelectedSize(
                userId, request.coffeeId, request.selectedSize)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "Coffee with this size already in favorites"))
        }

        val favoriteCoffee = FavoriteCoffee(
            userId = userId,
            coffeeId = request.coffeeId,
            selectedSize = request.selectedSize
        )

        favoriteCoffeeRepository.save(favoriteCoffee)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("message" to "Coffee added to favorites"))
    }

    @Transactional
    fun removeFromFavorites(userId: Long, coffeeId: Int, size: String?): ResponseEntity<Any> {
        if (size != null) {
            if (!favoriteCoffeeRepository.existsByUserIdAndCoffeeIdAndSelectedSize(userId, coffeeId, size)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("message" to "Coffee with this size not in favorites"))
            }

            favoriteCoffeeRepository.deleteByUserIdAndCoffeeIdAndSelectedSize(userId, coffeeId, size)
        } else {
            val favoritesForCoffee = favoriteCoffeeRepository.findAllByUserIdAndCoffeeId(userId, coffeeId)
            if (favoritesForCoffee.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("message" to "Coffee not in favorites"))
            }

            favoriteCoffeeRepository.deleteAllByUserIdAndCoffeeId(userId, coffeeId)
        }

        return ResponseEntity.ok(mapOf("message" to "Coffee removed from favorites"))
    }
}