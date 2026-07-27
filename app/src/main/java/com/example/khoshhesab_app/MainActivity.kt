package com.example.khoshhesab_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.khoshhesab_app.data.datastore.TokenManager
import com.example.khoshhesab_app.data.repository.AuthRepository
import com.example.khoshhesab_app.data.repository.GroupRepository
import com.example.khoshhesab_app.ui.auth.AuthViewModel
import com.example.khoshhesab_app.ui.auth.ForgotPasswordScreen
import com.example.khoshhesab_app.ui.auth.LoginScreen
import com.example.khoshhesab_app.ui.auth.RegisterScreen
import com.example.khoshhesab_app.ui.groups.GroupDetailScreen
import com.example.khoshhesab_app.ui.home.HomeScreen
import com.example.khoshhesab_app.ui.home.HomeViewModel
import com.example.khoshhesab_app.ui.navigation.Routes
import com.example.khoshhesab_app.ui.profile.ProfileScreen
import com.example.khoshhesab_app.ui.profile.ProfileViewModel
import com.example.khoshhesab_app.ui.theme.KhoshhesabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhoshhesabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tokenManager = TokenManager(this)
                    val authRepository = AuthRepository(tokenManager)
                    val groupRepository = GroupRepository(tokenManager)

                    val authViewModel: AuthViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return AuthViewModel(authRepository) as T
                            }
                        }
                    )

                    val homeViewModel: HomeViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return HomeViewModel(groupRepository, authRepository) as T
                            }
                        }
                    )

                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return ProfileViewModel(authRepository, groupRepository) as T
                            }
                        }
                    )

                    val navController = rememberNavController()
                    var isLoggedIn by remember { mutableStateOf(false) }

                    // ✅ چک کردن لاگین
                    LaunchedEffect(Unit) {
                        isLoggedIn = authRepository.isLoggedIn()
                    }

                    // ✅ گوش دادن به تغییرات لاگین
                    LaunchedEffect(authViewModel.isLoggedIn) {
                        isLoggedIn = authViewModel.isLoggedIn.value
                    }

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Routes.HOME else Routes.LOGIN
                    ) {
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(Routes.REGISTER)
                                },
                                onNavigateToForgotPassword = {
                                    navController.navigate(Routes.FORGOT_PASSWORD)
                                },
                                viewModel = authViewModel
                            )
                        }

                        composable(Routes.REGISTER) {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                    navController.navigate(Routes.LOGIN)
                                },
                                onRegisterSuccess = {
                                    isLoggedIn = true
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                viewModel = authViewModel
                            )
                        }

                        composable(Routes.FORGOT_PASSWORD) {
                            ForgotPasswordScreen(
                                onBack = { navController.popBackStack() },
                                onSuccess = {
                                    navController.popBackStack()
                                },
                                viewModel = authViewModel
                            )
                        }

                        composable(Routes.HOME) {
                            HomeScreen(
                                onLogout = {
                                    authViewModel.logout()
                                    isLoggedIn = false
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                    }
                                },
                                onGroupClick = { groupId ->
                                    navController.navigate("groupDetail/$groupId")
                                },
                                onCreateGroup = {
                                    // TODO: Create Group
                                },
                                onProfileClick = {
                                    navController.navigate(Routes.PROFILE)
                                },
                                viewModel = homeViewModel
                            )
                        }

                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    authViewModel.logout()
                                    isLoggedIn = false
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                    }
                                },
                                viewModel = profileViewModel
                            )
                        }

                        composable(
                            route = Routes.GROUP_DETAIL,
                            arguments = listOf(
                                navArgument("groupId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
                            GroupDetailScreen(
                                groupId = groupId,
                                onBack = { navController.popBackStack() },
                                onAddExpense = {
                                    // TODO: Add Expense
                                },
                                onSettle = {
                                    // TODO: Settlement
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}