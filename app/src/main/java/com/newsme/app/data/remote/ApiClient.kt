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

    suspend fun login(email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("password", password).build()
            val request = Request.Builder().url(BASE_URL + "login.php").post(form).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            ApiResponse(
                success = json.optBoolean("success", json.optBoolean("status", false)),
                message = json.optString("message", body),
                token = json.optString("token", null)
            )
        } catch (e: Exception) {
            ApiResponse(false, e.message ?: "خطأ اتصال")
        }
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("name", name).add("email", email).add("password", password).build()
            val request = Request.Builder().url(BASE_URL + "register.php").post(form).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) {
            ApiResponse(false, e.message ?: "خطأ اتصال")
        }
    }

    suspend fun sendOtp(email: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).build()
            val request = Request.Builder().url(BASE_URL + "send_email.php").post(form).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body))
        } catch (e: Exception) {
            ApiResponse(false, e.message ?: "خطأ")
        }
    }

    suspend fun verify(email: String, otp: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder().add("email", email).add("otp", otp).add("code", otp).build()
            val request = Request.Builder().url(BASE_URL + "verification.php").post(form).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            ApiResponse(json.optBoolean("success", false), json.optString("message", body), json.optString("token", null))
        } catch (e: Exception) {
            ApiResponse(false, e.message ?: "خطأ")
        }
    }
}
