package com.unitytests.virtumarttest.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val productName: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float?= null,
    val description: String?= null,
    val colors: List<String>?= null,
    val size: List<String>?= null,
    val imageURLs: List<String>

): Parcelable {
    constructor(): this("0", "", "", 0f, imageURLs= emptyList())
}