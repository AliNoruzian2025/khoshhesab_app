package com.example.khoshhesab_app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khoshhesab_app.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val OTP_TIME = 120
    }

    // ===== وضعیت‌های اصلی =====
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent

    private val _otpVerified = MutableStateFlow(false)
    val otpVerified: StateFlow<Boolean> = _otpVerified

    private val _requiresName = MutableStateFlow(false)
    val requiresName: StateFlow<Boolean> = _requiresName

    private val _tempMobile = MutableStateFlow("")
    val tempMobile: StateFlow<String> = _tempMobile

    private val _userExists = MutableStateFlow(false)
    val userExists: StateFlow<Boolean> = _userExists

    private val _goToLogin = MutableStateFlow(false)
    val goToLogin: StateFlow<Boolean> = _goToLogin

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    // ===== تایمر =====
    private val _timer = MutableStateFlow(OTP_TIME)
    val timer: StateFlow<Int> = _timer

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private var timerJob: kotlinx.coroutines.Job? = null

    // ===== حالت نمایش OTP =====
    private val _showOtpField = MutableStateFlow(false)
    val showOtpField: StateFlow<Boolean> = _showOtpField

    private val _isPhoneEditable = MutableStateFlow(true)
    val isPhoneEditable: StateFlow<Boolean> = _isPhoneEditable

    // =====================================================
    // ===== متدهای تایمر =====
    // =====================================================

    fun startTimer() {
        try {
            Log.d(TAG, "startTimer called")
            resetTimer()
            _isTimerRunning.value = true
            _showOtpField.value = true
            _isPhoneEditable.value = false
            _otpSent.value = true

            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                var remaining = OTP_TIME
                while (remaining > 0 && _isTimerRunning.value) {
                    _timer.value = remaining
                    delay(1000)
                    remaining--
                }
                if (remaining <= 0) {
                    _timer.value = 0
                    _isTimerRunning.value = false
                    _otp.value = ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "startTimer error", e)
        }
    }

    fun stopTimer() {
        try {
            Log.d(TAG, "stopTimer called")
            _isTimerRunning.value = false
            timerJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "stopTimer error", e)
        }
    }

    fun resetTimer() {
        try {
            stopTimer()
            _timer.value = OTP_TIME
        } catch (e: Exception) {
            Log.e(TAG, "resetTimer error", e)
        }
    }

    fun resetOtpStateForRegister() {
        try {
            Log.d(TAG, "resetOtpStateForRegister called")
            _showOtpField.value = false
            _isPhoneEditable.value = true
            _timer.value = OTP_TIME
            _isTimerRunning.value = false
            _errorMessage.value = null
            _otp.value = ""
            _otpSent.value = false
            timerJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "resetOtpStateForRegister error", e)
        }
    }

    fun resetOtpStateForForgotPassword() {
        try {
            Log.d(TAG, "resetOtpStateForForgotPassword called")
            _showOtpField.value = true
            _isPhoneEditable.value = false
            _timer.value = OTP_TIME
            _isTimerRunning.value = false
            _errorMessage.value = null
            _otp.value = ""
            _otpSent.value = true
            timerJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "resetOtpStateForForgotPassword error", e)
        }
    }

    fun resetRegisterState() {
        try {
            Log.d(TAG, "resetRegisterState called")
            _tempMobile.value = ""
            _otpSent.value = false
            _showOtpField.value = false
            _requiresName.value = false
            _isPhoneEditable.value = true
            _timer.value = OTP_TIME
            _errorMessage.value = null
            _userExists.value = false
            _goToLogin.value = false
            _otpVerified.value = false
            _otp.value = ""
            stopTimer()
        } catch (e: Exception) {
            Log.e(TAG, "resetRegisterState error", e)
        }
    }

    fun resetState() {
        try {
            _otpSent.value = false
            _otpVerified.value = false
            _requiresName.value = false
            _errorMessage.value = null
            _tempMobile.value = ""
            _userExists.value = false
            _goToLogin.value = false
            _otp.value = ""
            resetTimer()
        } catch (e: Exception) {
            Log.e(TAG, "resetState error", e)
        }
    }

    // =====================================================
    // ===== ارسال OTP =====
    // =====================================================

    fun sendOtp(phone: String) {
        try {
            Log.d(TAG, "sendOtp called with phone: $phone")
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val response = authRepository.sendOtp(phone)
                    Log.d(TAG, "sendOtp response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "sendOtp successful")
                        _otpSent.value = true
                        _tempMobile.value = phone
                        _otp.value = ""
                        startTimer()
                        _errorMessage.value = "کد تایید ارسال شد"
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "sendOtp error body: $errorBody")
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")
                            _errorMessage.value = errorJson.optString("message", "خطا در ارسال کد")
                        } catch (e: Exception) {
                            _errorMessage.value = "خطا در ارسال کد"
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "sendOtp error", e)
                    _errorMessage.value = "خطا در ارتباط با سرور: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendOtp outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== بررسی کاربر برای ثبت‌نام =====
    // =====================================================

    fun checkUserForRegister(phone: String) {
        try {
            Log.d(TAG, "checkUserForRegister called with phone: $phone")
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val exists = authRepository.checkUserExists(phone)
                    Log.d(TAG, "checkUserForRegister exists=$exists")

                    if (exists) {
                        _errorMessage.value = "این شماره قبلاً ثبت نام کرده است"
                        _isLoading.value = false
                    } else {
                        sendOtp(phone)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "checkUserForRegister error", e)
                    _errorMessage.value = "خطا در بررسی کاربر"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUserForRegister outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== بررسی کاربر برای فراموشی رمز =====
    // =====================================================

    fun checkUserForForgotPassword(phone: String) {
        try {
            Log.d(TAG, "checkUserForForgotPassword called with phone: $phone")
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val exists = authRepository.checkUserExists(phone)
                    Log.d(TAG, "checkUserForForgotPassword exists=$exists")

                    if (exists) {
                        sendOtp(phone)
                    } else {
                        _errorMessage.value = "شماره شما در سیستم ثبت نشده است"
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "checkUserForForgotPassword error", e)
                    _errorMessage.value = "خطا در بررسی کاربر"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUserForForgotPassword outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    fun clearUserExists() {
        _userExists.value = false
    }

    // =====================================================
    // ===== تأیید کد برای ثبت‌نام =====
    // =====================================================

    fun verifyOtpForRegister(otp: String) {
        try {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "verifyOtpForRegister called with otp: $otp")
                    _isLoading.value = true
                    _errorMessage.value = null

                    val mobile = _tempMobile.value
                    if (mobile.isEmpty()) {
                        _errorMessage.value = "شماره موبایل وارد نشده است"
                        _isLoading.value = false
                        return@launch
                    }

                    val response = authRepository.verifyOtp(mobile, otp)
                    Log.d(TAG, "verifyOtpForRegister response code: ${response.code()}")
                    Log.d(TAG, "verifyOtpForRegister response body: ${response.body()}")

                    if (response.isSuccessful) {
                        val verifyResponse = response.body()
                        if (verifyResponse != null) {
                            if (verifyResponse.exists && verifyResponse.completed) {
                                _goToLogin.value = true
                                _errorMessage.value = "شما قبلاً ثبت‌نام کرده‌اید. لطفاً وارد شوید."
                                stopTimer()
                                resetOtpStateForRegister()
                            } else {
                                _requiresName.value = true
                                _otpVerified.value = true
                                _otp.value = otp
                                stopTimer()
                                _otpSent.value = false
                                _showOtpField.value = false
                                _errorMessage.value = "کد با موفقیت تأیید شد"
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "verifyOtpForRegister error body: $errorBody")
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")
                            _errorMessage.value = errorJson.optString("message", "کد تأیید اشتباه است")
                        } catch (e: Exception) {
                            _errorMessage.value = "کد تأیید اشتباه است"
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "verifyOtpForRegister exception", e)
                    _errorMessage.value = "خطا در ارتباط با سرور"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtpForRegister outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== تأیید کد برای فراموشی رمز =====
    // =====================================================

    fun verifyOtpForForgotPassword(otp: String) {
        try {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "verifyOtpForForgotPassword called with otp: $otp")
                    _isLoading.value = true
                    _errorMessage.value = null

                    val mobile = _tempMobile.value
                    if (mobile.isEmpty()) {
                        _errorMessage.value = "شماره موبایل وارد نشده است"
                        _isLoading.value = false
                        return@launch
                    }

                    val response = authRepository.verifyOtp(mobile, otp)
                    Log.d(TAG, "verifyOtpForForgotPassword response code: ${response.code()}")
                    Log.d(TAG, "verifyOtpForForgotPassword response body: ${response.body()}")

                    if (response.isSuccessful) {
                        val verifyResponse = response.body()
                        if (verifyResponse != null) {
                            if (verifyResponse.exists && verifyResponse.completed) {
                                _otpVerified.value = true
                                _otp.value = otp
                                stopTimer()
                                _otpSent.value = false
                                _showOtpField.value = false
                                _errorMessage.value = "کد با موفقیت تأیید شد"
                            } else {
                                _errorMessage.value = "کاربری با این شماره یافت نشد"
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "verifyOtpForForgotPassword error body: $errorBody")
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")
                            _errorMessage.value = errorJson.optString("message", "کد تأیید اشتباه است")
                        } catch (e: Exception) {
                            _errorMessage.value = "کد تأیید اشتباه است"
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "verifyOtpForForgotPassword exception", e)
                    _errorMessage.value = "خطا در ارتباط با سرور"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtpForForgotPassword outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    fun clearOtpVerified() {
        _otpVerified.value = false
    }

    fun clearGoToLogin() {
        _goToLogin.value = false
    }

    // =====================================================
    // ===== ورود با رمز عبور =====
    // =====================================================

    fun loginWithPassword(phone: String, password: String) {
        try {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val response = authRepository.loginWithPassword(phone, password)
                    Log.d(TAG, "loginWithPassword response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            authRepository.saveUserData(loginResponse)
                            _isLoggedIn.value = true
                            _errorMessage.value = "ورود موفق"
                        }
                    } else {
                        when (response.code()) {
                            404 -> _errorMessage.value = "حساب کاربری با این شماره یافت نشد"
                            401 -> _errorMessage.value = "رمز عبور اشتباه است"
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                try {
                                    val errorJson = JSONObject(errorBody ?: "{}")
                                    _errorMessage.value = errorJson.optString("message", "خطا در ورود")
                                } catch (e: Exception) {
                                    _errorMessage.value = "خطا در ورود (کد ${response.code()})"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "loginWithPassword error", e)
                    _errorMessage.value = "خطا در ارتباط با سرور"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loginWithPassword outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== ثبت‌نام =====
    // =====================================================

    fun registerUser(phone: String, otp: String, name: String, password: String) {
        try {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    Log.d(TAG, "registerUser: phone=$phone, name=$name")
                    val response = authRepository.register(phone, otp, name, password)
                    Log.d(TAG, "register response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            authRepository.saveUserData(loginResponse)
                            _isLoggedIn.value = true
                            _otp.value = ""
                            _errorMessage.value = "ثبت نام با موفقیت انجام شد"
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "register error body: $errorBody")
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")
                            _errorMessage.value = errorJson.optString("message", "خطا در ثبت‌نام")
                        } catch (e: Exception) {
                            _errorMessage.value = "خطا در ثبت‌نام (کد ${response.code()})"
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "registerUser exception", e)
                    _errorMessage.value = "خطا در ارتباط با سرور: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "registerUser outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== بازنشانی رمز عبور =====
    // =====================================================

    fun resetPassword(phone: String, otp: String, newPassword: String) {
        try {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    Log.d(TAG, "resetPassword: phone=$phone")
                    val response = authRepository.resetPassword(phone, otp, newPassword)
                    Log.d(TAG, "resetPassword response code: ${response.code()}")

                    if (response.isSuccessful) {
                        _errorMessage.value = "رمز عبور با موفقیت تغییر کرد"
                        _otp.value = ""
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "resetPassword error body: $errorBody")
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")
                            _errorMessage.value = errorJson.optString("message", "خطا در تغییر رمز")
                        } catch (e: Exception) {
                            _errorMessage.value = "خطا در تغییر رمز عبور"
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "resetPassword exception", e)
                    _errorMessage.value = "خطا در ارتباط با سرور"
                } finally {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "resetPassword outer error", e)
            _errorMessage.value = "خطا: ${e.message}"
        }
    }

    // =====================================================
    // ===== متدهای کمکی =====
    // =====================================================

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun logout() {
        try {
            viewModelScope.launch {
                try {
                    authRepository.logout()
                    authRepository.clearUserData()
                    _isLoggedIn.value = false
                    _tempMobile.value = ""
                    _otp.value = ""
                } catch (e: Exception) {
                    Log.e(TAG, "logout error", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "logout outer error", e)
        }
    }

    fun checkLoginStatus() {
        try {
            viewModelScope.launch {
                try {
                    _isLoggedIn.value = authRepository.isLoggedIn()
                } catch (e: Exception) {
                    Log.e(TAG, "checkLoginStatus error", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkLoginStatus outer error", e)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}