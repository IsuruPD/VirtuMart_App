package com.unitytests.virtumarttest.data

data class User(
    val id: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val imagePath: String = "",
    val nic: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val gender: String = "",
    val dob: String = ""
) {
    constructor() : this("", "", "", "")
}
