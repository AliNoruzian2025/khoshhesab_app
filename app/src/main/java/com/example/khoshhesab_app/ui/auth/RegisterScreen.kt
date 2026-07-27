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
fun RegisterScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    var step by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resetRegisterState()
        viewModel.clearGoToLogin()
        viewModel.clearError()
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
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val otpVerified by viewModel.otpVerified.collectAsState()
    val timer by viewModel.timer.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val userExists by viewModel.userExists.collectAsState()
    val showOtpField by viewModel.showOtpField.collectAsState()
    val isPhoneEditable by viewModel.isPhoneEditable.collectAsState()
    val tempMobile by viewModel.tempMobile.collectAsState()
    val requiresName by viewModel.requiresName.collectAsState()
    val goToLogin by viewModel.goToLogin.collectAsState()
    val viewModelOtp by viewModel.otp.collectAsState()

    LaunchedEffect(requiresName) {
        if (requiresName) {
            step = 2
            otp = viewModelOtp
            errorMessage = null
        }
    }

    LaunchedEffect(otpVerified) {
        if (otpVerified) {
            step = 2
            otp = viewModelOtp
            viewModel.clearOtpVerified()
            errorMessage = null
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(goToLogin) {
        if (goToLogin) {
            viewModel.clearGoToLogin()
            onNavigateToLogin()
        }
    }

    LaunchedEffect(userExists) {
        if (userExists) {
            errorMessage = "شما قبلاً ثبت‌نام کرده‌اید. لطفاً وارد شوید."
            delay(2000)
            errorMessage = null
            viewModel.clearUserExists()
            onNavigateToLogin()
        }
    }

    LaunchedEffect(viewModelError) {
        if (viewModelError != null) {
            errorMessage = viewModelError
            delay(3000)
            errorMessage = null
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت‌نام", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetRegisterState()
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
                text = if (step == 1) "ایجاد حساب کاربری" else "تکمیل اطلاعات",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (step == 1) "شماره موبایل خود را وارد کنید" else "اطلاعات خود را تکمیل کنید",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
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

                Spacer(modifier = Modifier.height(12.dp))

                if (!otpSent || !isTimerRunning) {
                    Button(
                        onClick = {
                            if (phone.length == 11) {
                                viewModel.checkUserForRegister(phone)
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
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("ارسال کد تأیید")
                        }
                    }
                }

                if (showOtpField) {
                    Spacer(modifier = Modifier.height(12.dp))

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
                                    viewModel.checkUserForRegister(phone)
                                    otp = ""
                                } else {
                                    errorMessage = "شماره موبایل باید ۱۱ رقم باشد"
                                    scope.launch {
                                        delay(2000)
                                        errorMessage = null
                                    }
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
                                    viewModel.verifyOtpForRegister(otp)
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

            } else {
                Text(
                    text = "شماره: $tempMobile",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی") },
                    placeholder = { Text("علی رضایی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور") },
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
                            name.isBlank() -> {
                                errorMessage = "لطفاً نام و نام خانوادگی را وارد کنید"
                                scope.launch { delay(2000); errorMessage = null }
                            }
                            password.length < 6 -> {
                                errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد"
                                scope.launch { delay(2000); errorMessage = null }
                            }
                            password != confirmPassword -> {
                                errorMessage = "رمز عبور و تکرار آن مطابقت ندارند"
                                scope.launch { delay(2000); errorMessage = null }
                            }
                            else -> {
                                viewModel.registerUser(
                                    phone = tempMobile,
                                    otp = otp,
                                    name = name,
                                    password = password
                                )
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
                        Text("ثبت‌نام")
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

            if (step == 1 && !otpSent) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.clearError(); onBack() }) {
                    Text("قبلاً حساب دارید؟ وارد شوید")
                }
            }
        }
    }
}