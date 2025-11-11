package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.CoffeeType

interface CoffeeTypeRepository : JpaRepository<CoffeeType, Int> {}