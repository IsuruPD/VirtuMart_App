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
    //Remove the comments when running
    /*else if(!(password.contains("[A-Z]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one uppercase letter!")
    }else if(!(password.contains("[a-z]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one lowercase letter!")
    }else if(!(password.contains("[0-9]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one number!")
    }else if(!(password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()))) {
        return RegisterValidation.Failed("The password should have at least one special character!")
    }
    */else{
        return RegisterValidation.Success
    }
}

/*if(!checkPasswordStrength(password))
        return RegisterValidation.Failed("Password should have a lowercase letter, uppercase letter, a number and a special character!")*/
/*
fun checkPasswordStrength(password: String): Boolean{
    var hasUpper= false
    var hasLower= false
    var hasNumber= false
    var hasSpecial= false

    for(char in password) {
        if (Character.isLowerCase(char)){
            hasLower = true
        }
        if (Character.isUpperCase(char)) {
            hasUpper = true
        }
        if (Character.isDigit(char)) {
            hasNumber = true
        }
        if (!Character.isLetterOrDigit(char)) {
            hasSpecial = true
        }
    }
    return (hasLower && hasUpper && hasNumber && hasSpecial)
}
*/