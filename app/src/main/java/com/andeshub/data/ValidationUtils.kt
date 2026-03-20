package com.andeshub.data

fun validateEmail(email: String): String? {
    return if (!email.endsWith("@uniandes.edu.co")) {
        "Email must end with @uniandes.edu.co"
    } else null
}

fun validatePassword(password: String): String? {
    return when {
        password.length < 8 -> "Password must have at least 8 characters"
        !password.any { it.isUpperCase() } -> "Password must have at least 1 uppercase letter"
        !password.any { it.isDigit() } -> "Password must have at least 1 number"
        else -> null
    }
}

fun validateFullName(fullName: String): String? {
    return if (fullName.trim().split(" ").size < 2) {
        "Please enter your full name"
    } else null
}

fun validateMajor(major: String): String? {
    return if (!ALLOWED_MAJORS.contains(major)) {
        "Please select a valid major"
    } else null
}