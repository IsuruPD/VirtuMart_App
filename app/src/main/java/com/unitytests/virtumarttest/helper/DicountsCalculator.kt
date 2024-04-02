package com.unitytests.virtumarttest.helper

fun Float?.getProductPrice(price: Float): Float{
    if(this == null){
        return price
    }
    val priceAfterDiscount = (1f-this) * price
    return priceAfterDiscount
}