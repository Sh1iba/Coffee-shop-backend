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
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createOrder(userId: Long, request: OrderRequest): ResponseEntity<Any> {
        userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")

        if (request.deliveryAddress.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delivery address is required")
        }

        if (request.items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No items selected for order")
        }

        val allCartItems = cartItemRepository.findAllByUserId(userId)
        if (allCartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty")
        }

        val selectedCartItems = allCartItems.filter { cartItem ->
            request.items.any { it.productId == cartItem.productId && it.selectedSize == cartItem.selectedSize }
        }

        if (selectedCartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Selected items not found in cart")
        }

        val order = orderRepository.save(
            Order(
                userId = userId,
                totalAmount = BigDecimal.ZERO,
                deliveryFee = request.deliveryFee,
                deliveryAddress = request.deliveryAddress,
                orderDate = LocalDateTime.now()
            )
        )

        var itemsAmount = BigDecimal.ZERO
        val orderItems = mutableListOf<OrderItem>()

        for (cartItem in selectedCartItems) {
            val product = cartItem.product ?: continue
            val variant = productVariantRepository.findAllByProductId(product.id)
                .find { it.size == cartItem.selectedSize } ?: continue

            val itemTotal = variant.price.multiply(BigDecimal.valueOf(cartItem.quantity.toLong()))
            itemsAmount = itemsAmount.add(itemTotal)

            orderItems.add(
                OrderItem(
                    order = order,
                    productName = product.name,
                    selectedSize = cartItem.selectedSize,
                    unitPrice = variant.price,
                    quantity = cartItem.quantity,
                    totalPrice = itemTotal
                )
            )

            cartItemRepository.deleteByUserIdAndProductIdAndSelectedSize(
                userId, cartItem.productId, cartItem.selectedSize
            )
        }

        val finalTotal = itemsAmount.add(request.deliveryFee)
        order.totalAmount = finalTotal
        orderRepository.save(order)
        orderItemRepository.saveAll(orderItems)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "message" to "Order created successfully",
                "orderId" to order.id,
                "totalAmount" to finalTotal,
                "deliveryFee" to request.deliveryFee,
                "itemsAmount" to itemsAmount
            )
        )
    }

    fun getOrderHistory(userId: Long): ResponseEntity<List<OrderResponse>> {
        val orders = orderRepository.findAllByUserIdOrderByOrderDateDesc(userId)
        return ResponseEntity.ok(orders.map { order ->
            val items = orderItemRepository.findAllByOrderId(order.id)
            OrderResponse(
                id = order.id,
                totalAmount = order.totalAmount,
                deliveryFee = order.deliveryFee,
                deliveryAddress = order.deliveryAddress,
                orderDate = order.orderDate,
                items = items.map { item ->
                    OrderItemResponse(
                        id = item.id,
                        productName = item.productName,
                        selectedSize = item.selectedSize,
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        totalPrice = item.totalPrice
                    )
                }
            )
        })
    }

    fun getOrderDetails(userId: Long, orderId: Long): ResponseEntity<Any> {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found")

        if (order.userId != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied")
        }

        val items = orderItemRepository.findAllByOrderId(order.id)
        return ResponseEntity.ok(
            OrderResponse(
                id = order.id,
                totalAmount = order.totalAmount,
                deliveryFee = order.deliveryFee,
                deliveryAddress = order.deliveryAddress,
                orderDate = order.orderDate,
                items = items.map { item ->
                    OrderItemResponse(
                        id = item.id,
                        productName = item.productName,
                        selectedSize = item.selectedSize,
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        totalPrice = item.totalPrice
                    )
                }
            )
        )
    }
}
