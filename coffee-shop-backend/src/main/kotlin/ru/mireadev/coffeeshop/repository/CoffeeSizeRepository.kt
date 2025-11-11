package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.CoffeeSize

interface CoffeeSizeRepository : JpaRepository<CoffeeSize, Int> {
    fun findAllByCoffeeId(coffeeId: Int): List<CoffeeSize>
}