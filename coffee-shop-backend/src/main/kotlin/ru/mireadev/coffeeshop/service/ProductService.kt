package ru.mireadev.coffeeshop.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.mireadev.coffeeshop.dto.ProductCategoryResponse
import ru.mireadev.coffeeshop.dto.ProductResponse
import ru.mireadev.coffeeshop.dto.ProductVariantResponse
import ru.mireadev.coffeeshop.repository.ProductCategoryRepository
import ru.mireadev.coffeeshop.repository.ProductRepository
import ru.mireadev.coffeeshop.repository.ProductVariantRepository

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val productVariantRepository: ProductVariantRepository
) {
    fun getAllCategories(): ResponseEntity<List<ProductCategoryResponse>> {
        return ResponseEntity.ok(productCategoryRepository.findAll().map {
            ProductCategoryResponse(id = it.id, type = it.type)
        })
    }

    fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
        return ResponseEntity.ok(productRepository.findAll().map { product ->
            ProductResponse(
                id = product.id,
                category = ProductCategoryResponse(id = product.category.id, type = product.category.type),
                name = product.name,
                description = product.description,
                imageName = product.imageName,
                variants = product.variants.map { variant ->
                    ProductVariantResponse(size = variant.size, price = variant.price.toFloat())
                }
            )
        })
    }
}
