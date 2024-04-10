package com.unitytests.virtumarttest.data

data class User(val firstname: String, val lastname: String, val email: String, val imagePath: String=""){

    constructor(): this("","","")

}