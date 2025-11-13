package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.FavoriteCoffee
import ru.mireadev.coffeeshop.entity.FavoriteCoffeeId

interface FavoriteCoffeeRepository : JpaRepository<FavoriteCoffee, FavoriteCoffeeId> {
    fun findAllByUserId(userId: Long): List<FavoriteCoffee>
    fun existsByUserIdAndCoffeeId(userId: Long, coffeeId: Int): Boolean
    fun deleteByUserIdAndCoffeeId(userId: Long, coffeeId: Int)
    fun findByUserIdAndCoffeeId(userId: Long, coffeeId: Int): FavoriteCoffee?
} 