package com.example.khoshhesab_app.data.repository

import android.util.Log
import com.example.khoshhesab_app.data.api.RetrofitInstance
import com.example.khoshhesab_app.data.datastore.TokenManager
import com.example.khoshhesab_app.data.model.CheckUserResponse
import com.example.khoshhesab_app.data.model.LoginResponse
import com.example.khoshhesab_app.data.model.VerifyOtpResponse
import com.example.khoshhesab_app.data.model.User
import kotlinx.coroutines.flow.first
import retrofit2.Response

class AuthRepository(
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    private val api = RetrofitInstance.api

    suspend fun sendOtp(mobile: String): Response<Map<String, String>> {
        return try {
            Log.d(TAG, "sendOtp: mobile=$mobile")
            val response = api.sendOtp(mobile)
            Log.d(TAG, "sendOtp response code: ${response.code()}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "sendOtp error", e)
            throw e
        }
    }

    suspend fun verifyOtp(mobile: String, otp: String): Response<VerifyOtpResponse> {
        return try {
            Log.d(TAG, "verifyOtp: mobile=$mobile, otp=$otp")
            val response = api.verifyOtp(mobile, otp)
            Log.d(TAG, "verifyOtp response code: ${response.code()}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp error", e)
            throw e
        }
    }

    suspend fun saveUserData(response: LoginResponse) {
        try {
            Log.d(TAG, "saveUserData: ${response.user.name}")
            tokenManager.saveUserData(
                token = response.token,
                userId = response.user.id.toString(),
                name = response.user.name,
                phone = response.user.phone
            )
        } catch (e: Exception) {
            Log.e(TAG, "saveUserData error", e)
            throw e
        }
    }

    suspend fun logout(): Response<Map<String, String>> {
        return try {
            val token = tokenManager.getTokenSync()
            if (token != null) {
                api.logout("Bearer $token")
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "logout error", e)
            throw e
        }
    }

    suspend fun clearUserData() {
        try {
            tokenManager.clear()
        } catch (e: Exception) {
            Log.e(TAG, "clearUserData error", e)
            throw e
        }
    }

    suspend fun getToken(): String? {
        return try {
            tokenManager.getTokenSync()
        } catch (e: Exception) {
            Log.e(TAG, "getToken error", e)
            null
        }
    }

    suspend fun getProfile(): Response<User> {
        return try {
            val token = tokenManager.getTokenSync()
            Log.d(TAG, "getProfile token: $token")
            if (token != null) {
                api.getProfile("Bearer $token")
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProfile error", e)
            throw e
        }
    }

    suspend fun updateProfile(name: String, avatar: String? = null): Response<User> {
        return try {
            val token = tokenManager.getTokenSync()
            if (token != null) {
                api.updateProfile("Bearer $token", name, avatar)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile error", e)
            throw e
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return try {
            tokenManager.isLoggedIn.first()
        } catch (e: Exception) {
            Log.e(TAG, "isLoggedIn error", e)
            false
        }
    }

    suspend fun loginWithPassword(phone: String, password: String): Response<LoginResponse> {
        return try {
            Log.d(TAG, "loginWithPassword: phone=$phone")
            api.loginWithPassword(phone, password)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithPassword error", e)
            throw e
        }
    }

    suspend fun register(phone: String, otp: String, name: String, password: String): Response<LoginResponse> {
        return try {
            Log.d(TAG, "register: phone=$phone, name=$name")
            api.register(phone, otp, name, password)
        } catch (e: Exception) {
            Log.e(TAG, "register error", e)
            throw e
        }
    }

    suspend fun resetPassword(phone: String, otp: String, newPassword: String): Response<Map<String, String>> {
        return try {
            Log.d(TAG, "resetPassword: phone=$phone")
            api.resetPassword(phone, otp, newPassword)
        } catch (e: Exception) {
            Log.e(TAG, "resetPassword error", e)
            throw e
        }
    }

    suspend fun checkUserExists(phone: String): Boolean {
        return try {
            Log.d(TAG, "checkUserExists: phone=$phone")
            val response = api.checkUserExists(phone)
            Log.d(TAG, "checkUserExists response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "checkUserExists body: $body")
                body?.exists ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUserExists error", e)
            false
        }
    }

    suspend fun checkUserExistsFull(phone: String): CheckUserResponse? {
        return try {
            Log.d(TAG, "checkUserExistsFull: phone=$phone")
            val response = api.checkUserExists(phone)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUserExistsFull error", e)
            null
        }
    }
}