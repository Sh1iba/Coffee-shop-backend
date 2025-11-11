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
                id = coffee.id
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

        if (favoriteCoffeeRepository.existsByUserIdAndCoffeeId(userId, request.coffeeId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "Coffee already in favorites"))
        }

        val favoriteCoffee = FavoriteCoffee(
            userId = userId,
            coffeeId = request.coffeeId
        )
        
        favoriteCoffeeRepository.save(favoriteCoffee)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("message" to "Coffee added to favorites"))
    }
    
    @Transactional
    fun removeFromFavorites(userId: Long, coffeeId: Int): ResponseEntity<Any> {
        // Check if in favorites
        if (!favoriteCoffeeRepository.existsByUserIdAndCoffeeId(userId, coffeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Coffee not in favorites"))
        }

        favoriteCoffeeRepository.deleteByUserIdAndCoffeeId(userId, coffeeId)
        
        return ResponseEntity.ok(mapOf("message" to "Coffee removed from favorites"))
    }
} 