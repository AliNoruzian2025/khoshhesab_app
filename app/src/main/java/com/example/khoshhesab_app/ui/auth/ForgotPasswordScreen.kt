package com.example.khoshhesab_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khoshhesab_app.ui.theme.Green
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var step by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.clearGoToLogin()
    }

    LaunchedEffect(viewModel.isTimerRunning) {
        if (!viewModel.isTimerRunning.value && viewModel.otpSent.value) {
            otp = ""
            errorMessage = "زمان کد تأیید به پایان رسید، مجدداً درخواست کنید"
            scope.launch {
                delay(2000)
                errorMessage = null
            }
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val otpVerified by viewModel.otpVerified.collectAsState()
    val timer by viewModel.timer.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val showOtpField by viewModel.showOtpField.collectAsState()
    val isPhoneEditable by viewModel.isPhoneEditable.collectAsState()
    val tempMobile by viewModel.tempMobile.collectAsState()
    val goToLogin by viewModel.goToLogin.collectAsState()

    LaunchedEffect(tempMobile) {
        if (tempMobile.isNotEmpty() && phone.isEmpty()) {
            phone = tempMobile
        }
    }

    LaunchedEffect(viewModelError) {
        if (viewModelError != null) {
            errorMessage = viewModelError
            if (viewModelError?.contains("ثبت نشده") == true ||
                viewModelError?.contains("یافت نشد") == true) {
                step = 1
                viewModel.resetState()
            }
            delay(3000)
            errorMessage = null
            viewModel.clearError()
        }
    }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            step = 2
        }
    }

    LaunchedEffect(otpVerified) {
        if (otpVerified) {
            step = 3
            viewModel.clearOtpVerified()
            errorMessage = null
        }
    }

    LaunchedEffect(goToLogin) {
        if (goToLogin) {
            viewModel.clearGoToLogin()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فراموشی رمز عبور", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (step) {
                    1 -> "شماره موبایل خود را وارد کنید"
                    2 -> "کد تأیید را وارد کنید"
                    else -> "رمز عبور جدید را وارد کنید"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tempMobile.isNotEmpty()) {
                Text(
                    text = "شماره: $tempMobile",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = "شماره موبایل خود را وارد کنید",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (step) {
                1 -> {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("شماره موبایل") },
                        placeholder = { Text("09123456789") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isPhoneEditable,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (phone.length == 11) {
                                viewModel.checkUserForForgotPassword(phone)
                            } else {
                                errorMessage = "شماره موبایل باید ۱۱ رقم باشد"
                                scope.launch {
                                    delay(2000)
                                    errorMessage = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && isPhoneEditable
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("ارسال کد تأیید")
                        }
                    }
                }

                2 -> {
                    if (showOtpField) {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it },
                            label = { Text("کد تأیید") },
                            placeholder = { Text("کد ۷ رقمی") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isTimerRunning) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "ارسال مجدد کد در $timer ثانیه",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    if (phone.length == 11) {
                                        viewModel.checkUserForForgotPassword(phone)
                                        otp = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ارسال مجدد کد")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isTimerRunning) {
                            Button(
                                onClick = {
                                    if (otp.length == 7) {
                                        viewModel.verifyOtpForForgotPassword(otp)
                                    } else {
                                        errorMessage = "کد باید ۷ رقم باشد"
                                        scope.launch {
                                            delay(2000)
                                            errorMessage = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Text("تأیید کد")
                                }
                            }
                        }
                    }
                }

                3 -> {
                    Text(
                        text = "رمز عبور جدید را وارد کنید",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("رمز عبور جدید") },
                        placeholder = { Text("حداقل ۶ کاراکتر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("تکرار رمز عبور") },
                        placeholder = { Text("رمز را دوباره وارد کنید") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            when {
                                newPassword.length < 6 -> {
                                    errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد"
                                    scope.launch { delay(2000); errorMessage = null }
                                }
                                newPassword != confirmPassword -> {
                                    errorMessage = "رمز عبور و تکرار آن مطابقت ندارند"
                                    scope.launch { delay(2000); errorMessage = null }
                                }
                                else -> {
                                    viewModel.resetPassword(tempMobile, otp, newPassword)
                                    successMessage = "رمز عبور با موفقیت تغییر کرد"
                                    scope.launch {
                                        delay(2000)
                                        onSuccess()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("تغییر رمز عبور")
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = if (errorMessage?.contains("موفق") == true)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = successMessage ?: "",
                    color = Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}