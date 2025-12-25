package com.example.demolition

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val age: String = "",
    val location: String = "",
    val gender: String = "",
    val studentClass: String = "",
    val section: String = "",
    val avatarId: String = "avatar1",  // Default to avatar1
    val name: String = ""  // Combined firstName + lastName
)
