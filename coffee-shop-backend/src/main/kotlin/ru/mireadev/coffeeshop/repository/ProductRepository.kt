package ru.mireadev.coffeeshop.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mireadev.coffeeshop.entity.Product

interface ProductRepository : JpaRepository<Product, Int>
