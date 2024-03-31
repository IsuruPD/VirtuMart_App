package com.unitytests.virtumarttest.data

data class CartProducts(
    val product: Product,
    val quantity: Int,
    val selectedColor: Int? = null,
    val selectedString: String? = null
) {
    constructor(): this(Product(), 1, null, null)
}