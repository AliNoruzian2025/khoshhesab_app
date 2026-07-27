package com.example.khoshhesab_app.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khoshhesab_app.data.model.Balance
import com.example.khoshhesab_app.data.model.Expense
import com.example.khoshhesab_app.data.model.Group
import com.example.khoshhesab_app.data.repository.GroupRepository
import com.example.khoshhesab_app.data.datastore.TokenManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Int,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onSettle: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: GroupDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GroupDetailViewModel(
                    groupRepository = GroupRepository(
                        TokenManager(context)
                    )
                ) as T
            }
        }
    )

    val group by viewModel.group.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllData(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "جزئیات گروه", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = "ثبت هزینه")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage ?: "خطا", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearError(); viewModel.loadAllData(groupId) }) {
                            Text("تلاش مجدد")
                        }
                    }
                }
            }
            group == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("گروه یافت نشد")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GroupInfoCard(group = group!!)
                    }

                    item {
                        Text("💰 مانده حساب اعضا", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (balances.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    "هیچ مانده‌ای ثبت نشده است",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(balances) { balance ->
                            BalanceItem(balance = balance)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("📋 هزینه‌ها", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (expenses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    "هیچ هزینه‌ای ثبت نشده است",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(expenses) { expense ->
                            ExpenseItem(expense = expense)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSettle,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تسویه حساب", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupInfoCard(group: Group) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(group.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                when (group.type) {
                    "dang" -> "⚡ دنگ (کوتاه‌مدت)"
                    "account" -> "📊 حساب (بلندمدت)"
                    else -> group.type
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            group.due_date?.let {
                Text("📅 سررسید: $it", fontSize = 14.sp)
            }
            Text("🔑 کد دعوت: ${group.invite_code}", fontSize = 14.sp)
            Text(
                if (group.is_active) "✅ فعال" else "❌ غیرفعال",
                fontSize = 14.sp,
                color = if (group.is_active)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun BalanceItem(balance: Balance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (balance.status) {
                "creditor" -> MaterialTheme.colorScheme.primaryContainer
                "debtor" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "کاربر",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    balance.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                when (balance.status) {
                    "creditor" -> "💰 +${balance.balance} (طلبکار)"
                    "debtor" -> "💸 -${balance.balance} (بدهکار)"
                    else -> "✅ تسویه"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (balance.status) {
                    "creditor" -> MaterialTheme.colorScheme.primary
                    "debtor" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(expense.description, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("${expense.amount} تومان", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "پرداخت‌کننده: ${expense.payer?.name ?: "نامشخص"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (expense.expense_date.isNotEmpty()) {
                Text(
                    "📅 ${expense.expense_date}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}