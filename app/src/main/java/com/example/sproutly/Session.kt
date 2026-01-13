package com.example.sproutly

object Session {
    var userId: Int = 0
    var userEmail: String = ""
    var userName: String = ""
    var userRole: String = "USER"

    fun isLoggedIn(): Boolean = userId > 0 && userEmail.isNotBlank()

    fun clear() {
        userId = 0
        userEmail = ""
        userName = ""
        userRole = "USER"
    }
}
