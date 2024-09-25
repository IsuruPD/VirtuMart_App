package com.unitytests.virtumarttest.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId: String,
    val productName: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float?= null,
    val description: String?= null,
    val quantity: Int=0,
    val colors: List<String>?= null,
    val size: List<String>?= null,
    val modelUrl: String?= null,
    val imageURLs: List<String>,
    val freeShipping: Boolean?=false

): Parcelable {
    constructor(): this("0", "", "", 0f, imageURLs= emptyList())
}