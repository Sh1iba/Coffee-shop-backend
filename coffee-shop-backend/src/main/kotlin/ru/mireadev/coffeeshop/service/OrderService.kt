package ru.mireadev.coffeeshop.service

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mireadev.coffeeshop.dto.*
import ru.mireadev.coffeeshop.entity.Order
import ru.mireadev.coffeeshop.entity.OrderItem
import ru.mireadev.coffeeshop.repository.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val coffeeCartRepository: CoffeeCartRepository,
    private val coffeeRepository: CoffeeRepository,
    private val coffeeSizeRepository: CoffeeSizeRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createOrder(userId: Long, request: OrderRequest): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("User not found")

        if (request.deliveryAddress.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Delivery address is required")
        }

        if (request.items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("No items selected for order")
        }

        val allCartItems = coffeeCartRepository.findAllByUserId(userId)
        if (allCartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cart is empty")
        }

        val selectedCartItems = allCartItems.filter { cartItem ->
            request.items.any { requestedItem ->
                requestedItem.coffeeId == cartItem.coffeeId &&
                        requestedItem.selectedSize == cartItem.selectedSize
            }
        }

        if (selectedCartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Selected items not found in cart")
        }

        var itemsAmount = BigDecimal.ZERO
        val orderItems = mutableListOf<OrderItem>()

        val order = Order(
            userId = userId,
            totalAmount = BigDecimal.ZERO,
            deliveryFee = request.deliveryFee,
            deliveryAddress = request.deliveryAddress,
            orderDate = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(order)

        for (cartItem in selectedCartItems) {
            val coffee = cartItem.coffee ?: continue

            val coffeeSize = coffeeSizeRepository.findAllByCoffeeId(coffee.id)
                .find { it.size == cartItem.selectedSize } ?: continue

            val itemTotal = coffeeSize.price.multiply(BigDecimal.valueOf(cartItem.quantity.toLong()))
            itemsAmount = itemsAmount.add(itemTotal)

            val orderItem = OrderItem(
                order = savedOrder,
                coffeeName = coffee.name,
                selectedSize = cartItem.selectedSize,
                unitPrice = coffeeSize.price,
                quantity = cartItem.quantity,
                totalPrice = itemTotal
            )
            orderItems.add(orderItem)

            coffeeCartRepository.deleteByUserIdAndCoffeeIdAndSelectedSize(
                userId, cartItem.coffeeId, cartItem.selectedSize
            )
        }

        val finalTotalAmount = itemsAmount.add(request.deliveryFee)

        savedOrder.totalAmount = finalTotalAmount
        orderRepository.save(savedOrder)

        orderItemRepository.saveAll(orderItems)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf(
                "message" to "Order created successfully",
                "orderId" to savedOrder.id,
                "totalAmount" to finalTotalAmount,
                "deliveryFee" to request.deliveryFee,
                "itemsAmount" to itemsAmount
            ))
    }

    fun getOrderHistory(userId: Long): ResponseEntity<List<OrderResponse>> {
        val orders = orderRepository.findAllByUserIdOrderByOrderDateDesc(userId)

        val orderResponses = orders.map { order ->
            val orderItems = orderItemRepository.findAllByOrderId(order.id)

            OrderResponse(
                id = order.id,
                totalAmount = order.totalAmount,
                deliveryFee = order.deliveryFee,
                deliveryAddress = order.deliveryAddress,
                orderDate = order.orderDate,
                items = orderItems.map { item ->
                    OrderItemResponse(
                        id = item.id,
                        coffeeName = item.coffeeName,
                        selectedSize = item.selectedSize,
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        totalPrice = item.totalPrice
                    )
                }
            )
        }

        return ResponseEntity.ok(orderResponses)
    }

    fun getOrderDetails(userId: Long, orderId: Long): ResponseEntity<Any> {
        val order = orderRepository.findById(orderId)
            .orElse(null) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Order not found")

        if (order.userId != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied")
        }

        val orderItems = orderItemRepository.findAllByOrderId(order.id)

        val orderResponse = OrderResponse(
            id = order.id,
            totalAmount = order.totalAmount,
            deliveryFee = order.deliveryFee,
            deliveryAddress = order.deliveryAddress,
            orderDate = order.orderDate,
            items = orderItems.map { item ->
                OrderItemResponse(
                    id = item.id,
                    coffeeName = item.coffeeName,
                    selectedSize = item.selectedSize,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice
                )
            }
        )

        return ResponseEntity.ok(orderResponse)
    }
}