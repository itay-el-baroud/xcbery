package com.newsme.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object ApiClient {
    private const val BASE_URL = "https://cccjvhhhlppkbev.rf.gd/"
    private val client = OkHttpClient()

    data class ApiResponse(val success: Boolean, val message: String, val token: String? = null)

    private fun makeRequest(url: String, form: FormBody): String {
        // محاولة اولى بهيدر متصفح
        var request = Request.Builder()
            .url(url)
            .post(form)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/120.0 Mobile")
            .header("Accept", "application/json")
            .build()

        var response = client.newCall(request).execute()
        var body = response.body?.string() ?: ""

        // لو رجع صفحة حماية rf.gd نطلع الكوكي __test ونعيد
        if (body.trim().startsWith("<") && body.contains("__test")) {
            val regex = Regex("__test=([A-Za-z0-9]+)")
            val cookie = regex.find(body)?.groupValues?.get(1) ?: ""
            if (cookie.isNotEmpty()) {
                request = Request.Builder()
                    .url(url)
                    .post(form)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/120.0 Mobile")
                    .header("Cookie", "__test=$cookie")
                    .header("Accept", "application/json")
                    .build()
                response = client.newCall(request).execute()
                body = response.body?.string() ?: ""
            }
        }
        return body
    }

    suspend fun login(email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("password", password).build()
            val body = makeRequest(BASE_URL + "login.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "الاستضافة منعت الطلب - افتح الموقع في المتصفح مرة واحدة الاول")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, "خطأ: ${e.message}") }
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("name", name).add("email", email).add("password", password).build()
            val body = makeRequest(BASE_URL + "register.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "الاستضافة منعت التسجيل")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, e.message ?: "خطأ") }
    }

    suspend fun sendOtp(email: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).build()
            val body = makeRequest(BASE_URL + "send_email.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "منع حماية")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body))
        } catch (e: Exception) { ApiResponse(false, e.message ?: "خطأ") }
    }

    suspend fun verify(email: String, otp: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("otp", otp).add("code", otp).build()
            val body = makeRequest(BASE_URL + "verification.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "منع حماية")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, e.message ?: "خطأ") }
    }
}
