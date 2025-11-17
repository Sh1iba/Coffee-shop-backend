package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.CoffeeCart
import ru.mireadev.coffeeshop.entity.CoffeeCartId

interface CoffeeCartRepository : JpaRepository<CoffeeCart, CoffeeCartId> {
    fun findAllByUserId(userId: Long): List<CoffeeCart>
    fun existsByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String): Boolean
    fun deleteByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String)
    fun findByUserIdAndCoffeeIdAndSelectedSize(userId: Long, coffeeId: Int, selectedSize: String): CoffeeCart?
    fun deleteAllByUserId(userId: Long)
}