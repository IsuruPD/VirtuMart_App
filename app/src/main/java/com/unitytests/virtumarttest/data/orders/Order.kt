package com.unitytests.virtumarttest.data.orders

import android.os.Parcelable
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.ShippingDetails
import kotlinx.parcelize.Parcelize
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random.Default.nextLong

@Parcelize
data class Order(
    val orderStatus: String,
    val orderTotal: Float,
    val orderItems: List<CartProducts>,
    val shippingAddress: ShippingDetails,
    val orderDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000_000) + currentTimeMillis()
): Parcelable{
    constructor():this("",0f, emptyList(),ShippingDetails())
}
