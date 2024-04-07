package com.unitytests.virtumarttest.data.orders

import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.ShippingDetails

data class Order(
    val orderStatus: String,
    val orderTotal: Float,
    val orderItems: List<CartProducts>,
    val shippingAddress: ShippingDetails
)
