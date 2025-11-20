package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.FavoriteCoffee
import ru.mireadev.coffeeshop.entity.FavoriteCoffeeId

interface FavoriteCoffeeRepository : JpaRepository<FavoriteCoffee, FavoriteCoffeeId> {
    fun findAllByUserId(userId: Long): List<FavoriteCoffee>


    fun existsByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String): Boolean
    fun deleteByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String)
    fun findByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String): FavoriteCoffee?
    fun existsByUserIdAndCoffeeId(userId: Long, coffeeId: Int): Boolean
    fun deleteAllByUserIdAndCoffeeId(userId: Long, coffeeId: Int)
    fun findAllByUserIdAndCoffeeId(userId: Long, coffeeId: Int): List<FavoriteCoffee>
}