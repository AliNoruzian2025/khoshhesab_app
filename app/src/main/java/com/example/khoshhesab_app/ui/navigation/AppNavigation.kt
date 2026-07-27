package com.example.khoshhesab_app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot-password"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val GROUP_DETAIL = "groupDetail/{groupId}"
}

fun getGroupDetailRoute(groupId: Int): String {
    return "groupDetail/$groupId"
}