package com.unitytests.virtumarttest.data

sealed class Category(val category: String) {
    object Kitchenware: Category("Kitchenware")
    object Cleaning: Category("Cleaning")
    object Entertainment: Category("Entertainment")
    object Health: Category("Health and Wellness")
    object HomeDecor: Category("Home Decor")
    object Laundry: Category("Laundry")
    object PersonalCare: Category("Personal Care")
    object AirConditioners: Category("Air Conditioners")

}