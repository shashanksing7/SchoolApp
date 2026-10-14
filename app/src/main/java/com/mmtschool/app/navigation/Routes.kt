package com.mmtschool.app.navigation

import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val SPLASH = "splash"
    const val SCHOOL_LIST = "school_list"

    // role_select/{schoolDocId}
    const val ROLE_SELECT = "role_select/{schoolDocId}"
    fun roleSelect(schoolDocId: String) = "role_select/${encode(schoolDocId)}"

    // roll_number/{schoolDocId}/{role}
    const val ROLL_NUMBER = "roll_number/{schoolDocId}/{role}"
    fun rollNumber(schoolDocId: String, role: String) =
        "roll_number/${encode(schoolDocId)}/${encode(role)}"

    // webview/{schoolDocId}/{schoolName}/{encodedUrl}/{isFirstLogin}
    const val WEBVIEW = "webview/{schoolDocId}/{schoolName}/{encodedUrl}/{isFirstLogin}"
    fun webview(
        schoolDocId: String,
        schoolName: String,
        url: String,
        isFirstLogin: Boolean
    ) = "webview/${encode(schoolDocId)}/${encode(schoolName)}/${encode(url)}/$isFirstLogin"

    const val SETTINGS = "settings"

    fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    fun decode(value: String): String = URLDecoder.decode(value, "UTF-8")
}
