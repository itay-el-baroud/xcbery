package com.newsme.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.CookieManager
import java.net.CookiePolicy

object ApiClient {
    private const val BASE_URL = "https://cccjvhhhlppkbev.rf.gd/"

    private val client = OkHttpClient.Builder()
       .cookieJar(JavaNetCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL)))
       .build()

    data class ApiResponse(val success: Boolean, val message: String, val token: String? = null)

    private fun makeRequest(url: String, form: FormBody): String {
        // اول محاولة
        var request = Request.Builder()
           .url(url)
           .post(form)
           .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
           .header("Accept", "application/json")
           .build()

        var response = client.newCall(request).execute()
        var body = response.body?.string()?: ""

        // لو رجع HTML بتاع الحماية نطلع منه كوكي __test ونعيد المحاولة
        if (body.trim().startsWith("<")) {
            val regex = Regex("__test=([a-zA-Z0-9]+)")
            val match = regex.find(body)
            if (match!= null) {
                val cookieValue = match.groupValues[1]
                // تاني محاولة بالكوكي
                request = Request.Builder()
                   .url(url)
                   .post(form)
                   .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                   .header("Cookie", "__test=$cookieValue")
                   .header("Accept", "application/json")
                   .build()
                response = client.newCall(request).execute()
                body = response.body?.string()?: ""
            }
        }
        return body
    }

    suspend fun login(email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("password", password).build()
            val body = makeRequest(BASE_URL + "login.php", form)
            if (body.trim().startsWith("<")) {
                return@withContext ApiResponse(false, "الاستضافة المجانية منعت الطلب - جرب تفتح الموقع في المتصفح مرة اولا: $body".take(200))
            }
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, "خطأ: ${e.message}\nالرد كان: ${e.toString().take(200)}") }
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("name", name).add("email", email).add("password", password).build()
            val body = makeRequest(BASE_URL + "register.php", form)
            if (body.trim().startsWith("<")) {
                return@withContext ApiResponse(false, "الاستضافة منعت التسجيل - افتح https://cccjvhhhlppkbev.rf.gd/ في المتصفح اولا")
            }
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, e.message?: "خطأ") }
    }

    suspend fun sendOtp(email: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).build()
            val body = makeRequest(BASE_URL + "send_email.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "منع من الاستضافة")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body))
        } catch (e: Exception) { ApiResponse(false, e.message?: "خطأ") }
    }

    suspend fun verify(email: String, otp: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("otp", otp).add("code", otp).build()
            val body = makeRequest(BASE_URL + "verification.php", form)
            if (body.trim().startsWith("<")) return@withContext ApiResponse(false, "منع من الاستضافة")
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) { ApiResponse(false, e.message?: "خطأ") }
    }
}
