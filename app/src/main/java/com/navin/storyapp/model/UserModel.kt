package com.navin.storyapp.model

data class UserModel(
    val userId: String,
    val name: String,
    val token: String,
    val isLogin: Boolean
)