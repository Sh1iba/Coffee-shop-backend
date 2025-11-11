package ru.mireadev.coffeeshop.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.mireadev.coffeeshop.dto.CoffeeResponse
import ru.mireadev.coffeeshop.dto.CoffeeTypeResponse
import ru.mireadev.coffeeshop.repository.CoffeeRepository
import ru.mireadev.coffeeshop.repository.CoffeeTypeRepository


@Service
class CoffeeService (
    private val coffeeRepository : CoffeeRepository,
    private val coffeeTypeRepository : CoffeeTypeRepository
){
    fun getAllCoffeeType() : ResponseEntity<List<CoffeeTypeResponse>> {
        val coffeeTypes = coffeeTypeRepository.findAll()
        return ResponseEntity.ok(coffeeTypes.map{
            CoffeeTypeResponse(
                id = it.id,
                type = it.type,
            )
        })
    }

    fun getAllCoffee(): ResponseEntity<List<CoffeeResponse>> {
        val coffees = coffeeRepository.findAll()
        return ResponseEntity.ok(coffees.map { it ->
            CoffeeResponse(
                id = it.id,
                type = it.type.let { type -> CoffeeTypeResponse(
                    id = type.id,
                    type = type.type,
                ) },
                name = it.name,
                description = it.description,
                price = it.price.toFloat(),
                imageName = it.imageName,
            )
        })
    }
}