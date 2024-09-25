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

fun getOrderStatuses(status: String): OrderStatuses{
    return when(status){
        "Ordered" ->{
            OrderStatuses.Ordered
        }
        "Cancelled" ->{
            OrderStatuses.Cancelled
        }
        "Processing" ->{
            OrderStatuses.Processing
        }
        "Shipped" ->{
            OrderStatuses.Shipped
        }
        "Delivered" ->{
            OrderStatuses.Delivered
        }
        "In Dispute" ->{
            OrderStatuses.InDispute
        }
        "Refunded" ->{
            OrderStatuses.Refunded
        }
        "Returned" ->{
            OrderStatuses.Returned
        }
        "Complete" ->{
            OrderStatuses.Complete
        }
        else-> OrderStatuses.Cancelled
    }
}
