package com.example.khoshhesab_app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khoshhesab_app.ui.theme.Green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadGroupStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "پروفایل",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ProfileHeader(
                        name = user?.name ?: "کاربر",
                        phone = user?.phone ?: ""
                    )
                }

                item {
                    ProfileSectionHeader(title = "ویرایش پروفایل")
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "نام",
                        subtitle = user?.name ?: "نام خود را وارد کنید",
                        onClick = { /* TODO: Edit Name */ }
                    )
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Phone,
                        title = "شماره موبایل",
                        subtitle = user?.phone ?: "",
                        onClick = { /* TODO: Change Phone */ }
                    )
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.PhotoCamera,
                        title = "عکس پروفایل",
                        subtitle = "تغییر عکس",
                        onClick = { /* TODO: Change Avatar */ }
                    )
                }

                item {
                    ProfileSectionHeader(title = "حساب کاربری")
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Lock,
                        title = "تغییر رمز عبور",
                        subtitle = "رمز عبور خود را تغییر دهید",
                        onClick = { /* TODO: Change Password */ }
                    )
                }

                item {
                    ProfileSectionHeader(title = "گروه‌ها")
                }

                item {
                    ProfileStatsCard(
                        totalGroups = viewModel.totalGroups.value,
                        totalDangs = viewModel.totalDangs.value,
                        totalAccounts = viewModel.totalAccounts.value
                    )
                }

                item {
                    ProfileSectionHeader(title = "درباره برنامه")
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Info,
                        title = "نسخه برنامه",
                        subtitle = "1.0.0",
                        onClick = { /* TODO: Show Version */ }
                    )
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Email,
                        title = "ارتباط با ما",
                        subtitle = "khoshhesab@gmail.com",
                        onClick = { /* TODO: Contact */ }
                    )
                }

                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Gavel,
                        title = "قوانین و مقررات",
                        subtitle = "مطالعه کنید",
                        onClick = { /* TODO: Show Rules */ }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = "خروج",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خروج از حساب",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, phone: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Green.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "آواتار",
                modifier = Modifier.size(48.dp),
                tint = Green
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = phone,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Green,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Green
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "بیشتر",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ProfileStatsCard(
    totalGroups: Int,
    totalDangs: Int,
    totalAccounts: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = totalGroups.toString(),
                label = "کل گروه‌ها",
                color = Green
            )
            StatItem(
                value = totalDangs.toString(),
                label = "دنگ",
                color = Color(0xFF4CAF50)
            )
            StatItem(
                value = totalAccounts.toString(),
                label = "حساب",
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}