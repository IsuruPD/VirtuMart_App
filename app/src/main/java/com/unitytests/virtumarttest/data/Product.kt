package com.unitytests.virtumarttest.data

data class Product(
    val id: String,
    val productName: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float?= null,
    val description: String?= null,
    val colors: List<String>?= null,
    val sizes: List<String>?= null,
    val imageURLs: List<String>

) {
    constructor(): this("0", "", "", 0f, imageURLs= emptyList())
}