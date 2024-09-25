package com.unitytests.virtumarttest.util

import android.util.Patterns
import java.util.regex.Pattern

fun validateEmail(email: String): RegisterValidation{
    if(email.isEmpty()){
        return RegisterValidation.Failed("The email cannot be empty!")
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
        return RegisterValidation.Failed("Incorrect email!")
    }else{
        return RegisterValidation.Success
    }
}

fun validatePassword(password: String): RegisterValidation{

    if (password.isEmpty()){
        return RegisterValidation.Failed("The password cannot be empty!")
    } else if((password.length<6)) {
        return RegisterValidation.Failed("Password should be more than 6 characters long!")
    }

    else if(!(password.contains("[A-Z]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one uppercase letter!")
    }else if(!(password.contains("[a-z]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one lowercase letter!")
    }else if(!(password.contains("[0-9]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one number!")
    }else if(!(password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one special character!")
    }
    else{
        return RegisterValidation.Success
    }
}

fun validateLoginEmail(email: String): RegisterValidation {
    if (email.isEmpty()) {
        return RegisterValidation.Failed("Email cannot be empty!")
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return RegisterValidation.Failed("Invalid email format!")
    }
    return RegisterValidation.Success
}

fun validateLoginPassword(password: String): RegisterValidation {
    if (password.isEmpty()) {
        return RegisterValidation.Failed("Password cannot be empty!")
    }
    return RegisterValidation.Success
}