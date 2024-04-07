package com.unitytests.virtumarttest.data.orders

sealed class OrderStatuses(val status: String){

    object Ordered: OrderStatuses("Ordered")
    object Cancelled: OrderStatuses("Cancelled")
    object Processing: OrderStatuses("Processing")
    object Shipped: OrderStatuses("Shipped")
    object Delivered: OrderStatuses("Delivered")
    object InDispute: OrderStatuses("In Dispute")
    object Refunded: OrderStatuses("Refunded")
    object Returned: OrderStatuses("Returned")
    object Complete: OrderStatuses("Complete")
}
