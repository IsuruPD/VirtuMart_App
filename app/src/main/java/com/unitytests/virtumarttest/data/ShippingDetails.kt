package com.unitytests.virtumarttest.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShippingDetails(
    val addressAlias: String,
    val receiverName: String,
    val address: String,
    val city: String,
    val district: String,
    val contact: String,
): Parcelable{
    constructor(): this("", "", "", "", "", "")
}
