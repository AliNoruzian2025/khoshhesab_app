package ir.khoshhesab.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.khoshhesab.app.AppViewModel
import ir.khoshhesab.app.data.*
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max

@Composable
fun PersonalFinanceScreen(vm: AppViewModel, onBack: () -> Unit) {
    var dashboard by remember { mutableStateOf(PersonalFinanceDashboard()) }
    var allAccounts by remember { mutableStateOf<List<PersonalAccount>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<PersonalTransaction>>(emptyList()) }
    var categories by remember { mutableStateOf(FinanceCategories()) }
    var tab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var accountEditor by remember { mutableStateOf<PersonalAccount?>(null) }
    var accountPendingDelete by remember { mutableStateOf<PersonalAccount?>(null) }
    var newAccount by remember { mutableStateOf(false) }
    var transactionEditor by remember { mutableStateOf<PersonalTransaction?>(null) }
    var newTransaction by remember { mutableStateOf(false) }
    var goalEditor by remember { mutableStateOf<SavingsGoal?>(null) }
    var newGoal by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        vm.request({ vm.api.personalFinanceDashboard() }) { dashboard = it }
        vm.request({ vm.api.personalAccounts(includeArchived = true) }, silent = true) { allAccounts = it }
        vm.request({ vm.api.personalTransactions() }, silent = true) { transactions = it }
        vm.request({ vm.api.financeCategories() }, silent = true) { categories = it }
    }

    Scaffold(
        topBar = { PageHeader("مدیریت مالی شخصی", onBack) },
        floatingActionButton = {
            if (tab != 0) FloatingActionButton(onClick = {
                when (tab) {
                    1 -> newAccount = true
                    2 -> newTransaction = true
                    3 -> newGoal = true
                }
            }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab) {
                listOf("خلاصه", "حساب‌ها", "تراکنش‌ها", "پس‌انداز").forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                }
            }
            when (tab) {
                0 -> FinanceOverview(dashboard) { tab = it }
                1 -> AccountList(allAccounts, onEdit = { accountEditor = it }, onDelete = { account ->
                    accountPendingDelete = account
                }, onRestore = { account ->
                    vm.request({ vm.api.updatePersonalAccount(account.id, PersonalAccountUpdateRequest(is_archived = false)) }) {
                        vm.showSuccess("حساب دوباره فعال شد"); refresh++
                    }
                })
                2 -> TransactionList(transactions, onEdit = { if (it.reference_type == null) transactionEditor = it }, onDelete = { item ->
                    vm.request({ vm.api.deletePersonalTransaction(item.id) }) {
                        vm.showSuccess(it.message); refresh++
                    }
                })
                3 -> GoalList(dashboard.goals, onEdit = { goalEditor = it }, onDelete = { goal ->
                    vm.request({ vm.api.deleteSavingsGoal(goal.id) }) {
                        vm.showSuccess(it.message); refresh++
                    }
                })
            }
        }
    }

    if (newAccount || accountEditor != null) AccountEditorDialog(
        current = accountEditor,
        onDismiss = { newAccount = false; accountEditor = null },
        onSave = { request ->
            if (accountEditor == null) vm.request({ vm.api.createPersonalAccount(request) }) {
                vm.showSuccess("حساب مالی ایجاد شد"); newAccount = false; refresh++
            } else vm.request({ vm.api.updatePersonalAccount(
                accountEditor!!.id,
                PersonalAccountUpdateRequest(name = request.name, type = request.type, note = request.note ?: "")
            ) }) {
                vm.showSuccess("حساب ویرایش شد"); accountEditor = null; refresh++
            }
        }
    )
    if (newTransaction || transactionEditor != null) TransactionEditorDialog(
        accounts = dashboard.accounts,
        categories = categories,
        current = transactionEditor,
        onDismiss = { newTransaction = false; transactionEditor = null },
        onSave = { request ->
            if (transactionEditor == null) vm.request({ vm.api.createPersonalTransaction(request) }) {
                vm.showSuccess("تراکنش ثبت شد"); newTransaction = false; refresh++
            } else vm.request({ vm.api.updatePersonalTransaction(transactionEditor!!.id, request) }) {
                vm.showSuccess("تراکنش ویرایش شد"); transactionEditor = null; refresh++
            }
        }
    )
    accountPendingDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountPendingDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف حساب مالی") },
            text = {
                Text("آیا از حذف «${account.name}» مطمئن هستید؟ اگر این حساب سابقه تراکنش داشته باشد برای حفظ اطلاعات مالی آرشیو می‌شود.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.request({ vm.api.deletePersonalAccount(account.id) }) {
                            vm.showSuccess(it.message)
                            accountPendingDelete = null
                            refresh++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("بله، حذف شود") }
            },
            dismissButton = { TextButton(onClick = { accountPendingDelete = null }) { Text("انصراف") } }
        )
    }
    if (newGoal || goalEditor != null) GoalEditorDialog(
        current = goalEditor,
        onDismiss = { newGoal = false; goalEditor = null },
        onSave = { request ->
            if (goalEditor == null) vm.request({ vm.api.createSavingsGoal(request) }) {
                vm.showSuccess(if (it.is_completed) "🎉 تبریک! به هدف پس‌انداز رسیدید" else "هدف پس‌انداز ثبت شد")
                newGoal = false; refresh++
            } else vm.request({ vm.api.updateSavingsGoal(goalEditor!!.id, request) }) {
                vm.showSuccess(if (it.is_completed && goalEditor?.is_completed == false) "🎉 تبریک! به هدف پس‌انداز رسیدید" else "هدف پس‌انداز ویرایش شد")
                goalEditor = null; refresh++
            }
        }
    )
}

@Composable
private fun FinanceOverview(data: PersonalFinanceDashboard, openTab: (Int) -> Unit) {
    var selectedReport by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF0B3978), contentColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مجموع موجودی", color = Color.White.copy(alpha = .8f))
                    Text(data.total_balance.toTomanText(), fontSize = 28.sp, fontWeight = FontWeight.Black)
                    HorizontalDivider(color = Color.White.copy(alpha = .2f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FinanceMiniValue("قابل خرج", data.spendable_balance, Color(0xFF8EF0C8))
                        FinanceMiniValue("خالص دارایی", data.net_worth, Color(0xFFFFD58A))
                    }
                }
            }
        }
        data.warning?.let { warning -> item {
            Surface(color = Color(0xFFFFE1E5), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFB42336)); Spacer(Modifier.width(8.dp))
                    Text(warning, color = Color(0xFF8C1D2C), fontWeight = FontWeight.Bold)
                }
            }
        } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("درآمد ماه", data.month_income, Color(0xFF008F68), Modifier.weight(1f))
                SummaryCard("هزینه ماه", data.month_expense, Color(0xFFD64A55), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("طلب‌ها", data.shared_receivable, Color(0xFF1769D2), Modifier.weight(1f))
                SummaryCard("بدهی‌ها", data.shared_debt, Color(0xFFE47717), Modifier.weight(1f))
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 7.dp)
            ) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("گزارش‌های مالی", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("بازه گزارش دلخواه را انتخاب کنید", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        FilterChip(selectedReport == "month", { selectedReport = "month" }, { Text("ماهانه") }, modifier = Modifier.weight(1f))
                        FilterChip(selectedReport == "six_months", { selectedReport = "six_months" }, { Text("۶ ماهه") }, modifier = Modifier.weight(1f))
                        FilterChip(selectedReport == "year", { selectedReport = "year" }, { Text("سالانه") }, modifier = Modifier.weight(1f))
                    }
                    if (selectedReport != null) {
                        OutlinedButton(onClick = { selectedReport = null }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.VisibilityOff, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("عدم نمایش گزارش انتخابی")
                        }
                    }
                }
            }
        }
        when (selectedReport) {
            "month" -> {
                item { SectionTitle("گزارش روزانه ماه جاری") }
                items(data.month_daily_trend) { day -> DailyTrendRow(day) }
            }
            "six_months" -> {
                item { SectionTitle("روند شش‌ماهه") }
                items(data.trend) { trend -> TrendRow(trend) }
            }
            "year" -> {
                item { SectionTitle("روند دوازده‌ماهه") }
                items(data.year_trend) { trend -> TrendRow(trend) }
            }
        }
        if (data.daily_trend.isNotEmpty()) {
            item { SectionTitle("هفت روز اخیر") }
            items(data.daily_trend) { day -> DailyTrendRow(day) }
        }
        if (data.category_expenses.isNotEmpty()) {
            item { SectionTitle("هزینه‌های این ماه بر اساس دسته‌بندی") }
            items(data.category_expenses) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(item.category, fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFE0E4)) {
                                Icon(Icons.Default.PieChart, null, tint = Color(0xFFD64A55), modifier = Modifier.padding(9.dp))
                            }
                        },
                        trailingContent = { Text(item.amount.toTomanText(), color = Color(0xFFD64A55), fontWeight = FontWeight.Black) }
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("آخرین تراکنش‌ها"); TextButton(onClick = { openTab(2) }) { Text("مشاهده همه") }
            }
        }
        if (data.recent_transactions.isEmpty()) item { EmptyFinance("هنوز تراکنشی ثبت نشده است") }
        else items(data.recent_transactions.take(5), key = { it.id }) { TransactionRow(it, {}, null) }
    }
}

@Composable private fun FinanceMiniValue(title: String, value: Long, color: Color) = Column {
    Text(title, color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
    Text(value.toTomanText(), color = color, fontWeight = FontWeight.Black)
}

@Composable private fun SummaryCard(title: String, value: Long, accent: Color, modifier: Modifier) {
    ElevatedCard(modifier, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 7.dp)) { Column(Modifier.padding(14.dp)) {
        Text(title, color = accent, fontWeight = FontWeight.Bold); Text(value.toTomanText(), fontWeight = FontWeight.Black, fontSize = 17.sp)
    } }
}

@Composable private fun TrendRow(item: FinanceTrend) {
    val maxValue = max(item.income, item.expense).coerceAtLeast(1)
    ElevatedCard(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${item.month}-01".toPersianDateText(), fontWeight = FontWeight.Bold)
        Text("درآمد ${item.income.toTomanText()}", color = Color(0xFF008F68), fontSize = 12.sp)
        LinearProgressIndicator(progress = { item.income.toFloat() / maxValue }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF00A17A))
        Text("هزینه ${item.expense.toTomanText()}", color = Color(0xFFD64A55), fontSize = 12.sp)
        LinearProgressIndicator(progress = { item.expense.toFloat() / maxValue }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFD64A55))
    } }
}

@Composable private fun DailyTrendRow(item: FinanceDailyTrend) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.date.toPersianDateText(), fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.End) {
                Text("درآمد ${item.income.toTomanText()}", color = Color(0xFF008F68), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("هزینه ${item.expense.toTomanText()}", color = Color(0xFFD64A55), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AccountList(
    items: List<PersonalAccount>,
    onEdit: (PersonalAccount) -> Unit,
    onDelete: (PersonalAccount) -> Unit,
    onRestore: (PersonalAccount) -> Unit
) {
    if (items.isEmpty()) { EmptyFinance("برای شروع، اولین حساب مالی خود را ایجاد کنید"); return }
    var actionAccount by remember { mutableStateOf<PersonalAccount?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.id }) { account ->
            val accent = if (account.is_archived) Color(0xFFD9364E) else accountAccent(account.type)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 7.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (account.is_archived) Color(0xFFFFE2E6) else MaterialTheme.colorScheme.surface,
                    contentColor = if (account.is_archived) Color(0xFF721426) else MaterialTheme.colorScheme.onSurface
                )
            ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(
                        Brush.linearGradient(listOf(accent, accent.copy(alpha = .62f)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(accountIcon(account.type), null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(account.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (account.is_archived) {
                        Surface(color = Color(0xFFD9364E), contentColor = Color.White, shape = RoundedCornerShape(50)) {
                            Text("حذف‌شده • قابل بازیابی", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else Text(accountTypeText(account.type), color = accent, fontWeight = FontWeight.Bold)
                    account.note?.takeIf { it.isNotBlank() }?.let {
                        Text(it, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Text(
                    account.balance.toTomanText(),
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { actionAccount = account },
                    modifier = Modifier.background(accent.copy(alpha = .12f), CircleShape)
                ) {
                    Icon(Icons.Default.MoreVert, "عملیات حساب", tint = accent)
                }
            }
        } }
    }
    actionAccount?.let { account ->
        val accent = if (account.is_archived) Color(0xFFD9364E) else accountAccent(account.type)
        ModalBottomSheet(onDismissRequest = { actionAccount = null }) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(
                            Brush.linearGradient(listOf(accent, accent.copy(alpha = .62f)))
                        ), contentAlignment = Alignment.Center
                    ) { Icon(accountIcon(account.type), null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(account.name, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(if (account.is_archived) "حساب آرشیوشده" else accountTypeText(account.type), color = accent, fontWeight = FontWeight.Bold)
                    }
                    Text(account.balance.toTomanText(), fontSize = 19.sp, fontWeight = FontWeight.Black)
                }
                account.note?.takeIf { it.isNotBlank() }?.let {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                        Text(it, Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
                if (account.is_archived) {
                    Button(
                        onClick = { actionAccount = null; onRestore(account) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07885A))
                    ) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(7.dp)); Text("بازیابی حساب") }
                } else {
                    Button(
                        onClick = { actionAccount = null; onEdit(account) },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(7.dp)); Text("ویرایش اطلاعات حساب") }
                    OutlinedButton(
                        onClick = { actionAccount = null; onDelete(account) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Icon(Icons.Default.DeleteOutline, null); Spacer(Modifier.width(7.dp)); Text("حذف حساب") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable private fun TransactionList(items: List<PersonalTransaction>, onEdit: (PersonalTransaction) -> Unit, onDelete: (PersonalTransaction) -> Unit) {
    if (items.isEmpty()) { EmptyFinance("هنوز درآمد یا هزینه‌ای ثبت نشده است"); return }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        items(items, key = { it.id }) { TransactionRow(it, { onEdit(it) }, { onDelete(it) }) }
    }
}

@Composable private fun TransactionRow(item: PersonalTransaction, onEdit: () -> Unit, onDelete: (() -> Unit)?) {
    val positive = item.type == "income" || (item.type == "adjustment" && item.amount > 0)
    val color = if (positive) Color(0xFF008F68) else Color(0xFFD64A55)
    ElevatedCard(Modifier.fillMaxWidth().clickable(enabled = item.reference_type == null, onClick = onEdit)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = .12f)) { Icon(
                if (positive) Icons.Default.SouthWest else if (item.type == "transfer") Icons.Default.SwapHoriz else Icons.Default.NorthEast,
                null, tint = color, modifier = Modifier.padding(10.dp)
            ) }
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold); Text("${item.account?.name.orEmpty()} • ${item.transaction_date.take(10).toPersianDateText()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.category?.let { Text(it, fontSize = 11.sp, color = color) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text((if (positive) "+" else "−") + abs(item.amount).toTomanText(), color = color, fontWeight = FontWeight.Black)
                if (item.reference_type == null && onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp)) }
                if (item.reference_type != null) Text("ثبت خودکار", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable private fun GoalList(items: List<SavingsGoal>, onEdit: (SavingsGoal) -> Unit, onDelete: (SavingsGoal) -> Unit) {
    if (items.isEmpty()) { EmptyFinance("یک هدف پس‌انداز بسازید و پیشرفت خود را دنبال کنید"); return }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.id }) { goal -> ElevatedCard(
            Modifier.fillMaxWidth().clickable { onEdit(goal) },
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (goal.is_completed) Color(0xFFD9F8E8) else MaterialTheme.colorScheme.surface,
                contentColor = if (goal.is_completed) Color(0xFF064E3B) else MaterialTheme.colorScheme.onSurface
            )
        ) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.is_completed) { Text("🎉", fontSize = 28.sp); Spacer(Modifier.width(8.dp)) }
                    Column {
                        Text(goal.title, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        if (goal.is_completed) Text("موفق شدی! هدف کامل شد", fontWeight = FontWeight.Bold, color = Color(0xFF087A55))
                    }
                }
                IconButton(onClick = { onDelete(goal) }) { Icon(Icons.Default.DeleteOutline, null) }
            }
            val progress = (goal.saved_amount.toFloat() / goal.target_amount.coerceAtLeast(1)).coerceIn(0f, 1f)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(9.dp))
            Text("${goal.saved_amount.toTomanText()} از ${goal.target_amount.toTomanText()}", fontWeight = FontWeight.Bold)
            goal.target_date?.let { Text("هدف تا ${it.take(10).toPersianDateText()}", fontSize = 12.sp) }
        } } }
    }
}

@Composable private fun EmptyFinance(text: String) = Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) { Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun SectionTitle(text: String) = Text(text, fontWeight = FontWeight.Black, fontSize = 18.sp)

@Composable
private fun AccountEditorDialog(current: PersonalAccount?, onDismiss: () -> Unit, onSave: (PersonalAccountRequest) -> Unit) {
    var name by remember(current) { mutableStateOf(current?.name.orEmpty()) }
    var type by remember(current) { mutableStateOf(current?.type ?: "card") }
    var opening by remember(current) { mutableStateOf(current?.opening_balance?.toString().orEmpty()) }
    var note by remember(current) { mutableStateOf(current?.note.orEmpty().take(100)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (current == null) "حساب مالی جدید" else "ویرایش حساب") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppField(name, { name = it }, "نام حساب")
        ChoiceDropdown("نوع حساب", type, listOf("card" to "کارت بانکی", "bank" to "حساب بانکی", "cash" to "پول نقد", "wallet" to "کیف پول", "other" to "سایر")) { type = it }
        if (current == null) AppField(opening, { opening = it.normalizeNumberDigits().filter(Char::isDigit) }, "موجودی اولیه (تومان)", KeyboardType.Number, visualTransformation = ThousandsSeparatorTransformation)
        AppField(note, { note = it.take(100) }, "کپشن حساب (حداکثر ۱۰۰ حرف)", singleLine = false)
        Text("${note.length}/100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }, confirmButton = { Button(onClick = { onSave(PersonalAccountRequest(name, type, opening.toLongOrNull() ?: current?.opening_balance ?: 0, note = note.ifBlank { null })) }, enabled = name.isNotBlank()) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } })
}

@Composable
private fun TransactionEditorDialog(accounts: List<PersonalAccount>, categories: FinanceCategories, current: PersonalTransaction?, onDismiss: () -> Unit, onSave: (PersonalTransactionRequest) -> Unit) {
    var type by remember(current) { mutableStateOf(current?.type ?: "expense") }
    var accountId by remember(current, accounts) { mutableLongStateOf(current?.account_id ?: accounts.firstOrNull()?.id ?: 0) }
    var destinationId by remember(current) { mutableLongStateOf(current?.destination_account_id ?: 0) }
    var amount by remember(current) { mutableStateOf(abs(current?.amount ?: 0).takeIf { it > 0 }?.toString().orEmpty()) }
    var negativeAdjustment by remember(current) { mutableStateOf((current?.amount ?: 0) < 0) }
    var title by remember(current) { mutableStateOf(current?.title.orEmpty()) }
    var category by remember(current) { mutableStateOf(current?.category.orEmpty()) }
    var note by remember(current) { mutableStateOf(current?.note.orEmpty()) }
    var date by remember(current) { mutableStateOf(current?.transaction_date?.take(10) ?: LocalDate.now().toString()) }
    val categoryItems = if (type == "income") categories.income else categories.expense
    val enteredAmount = amount.toLongOrNull() ?: 0
    val selectedBalance = accounts.firstOrNull { it.id == accountId }?.balance ?: 0
    val isOutgoing = type == "expense" || type == "transfer" || (type == "adjustment" && negativeAdjustment)
    val insufficient = current == null && isOutgoing && enteredAmount > selectedBalance
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (current == null) "تراکنش جدید" else "ویرایش تراکنش") }, text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { ChoiceDropdown("نوع تراکنش", type, listOf("expense" to "هزینه", "income" to "درآمد", "transfer" to "انتقال", "adjustment" to "اصلاح موجودی")) { type = it; category = "" } }
        item { AccountDropdown("حساب", accounts, accountId) { accountId = it } }
        if (type == "transfer") item { AccountDropdown("حساب مقصد", accounts.filter { it.id != accountId }, destinationId) { destinationId = it } }
        item { AppField(amount, { amount = it.normalizeNumberDigits().filter(Char::isDigit) }, "مبلغ (تومان)", KeyboardType.Number, visualTransformation = ThousandsSeparatorTransformation) }
        if (insufficient) item { Text("مبلغ هزینه از موجودی حساب انتخاب‌شده بیشتر است", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
        if (type == "adjustment") item { Row(verticalAlignment = Alignment.CenterVertically) { Switch(negativeAdjustment, { negativeAdjustment = it }); Text("کاهش موجودی") } }
        item { AppField(title, { title = it }, "عنوان") }
        if (type == "income" || type == "expense") item { ChoiceDropdown("دسته‌بندی", category, categoryItems.map { it to it }) { category = it } }
        item { PersianDateField(date, "تاریخ", maxDate = LocalDate.now()) { date = it ?: LocalDate.now().toString() } }
        item { AppField(note, { note = it }, "یادداشت (اختیاری)") }
    } }, confirmButton = { Button(onClick = {
        val raw = amount.toLongOrNull() ?: 0
        onSave(PersonalTransactionRequest(accountId, destinationId.takeIf { type == "transfer" && it > 0 }, type, if (type == "adjustment" && negativeAdjustment) -raw else raw, category.ifBlank { null }, title, note.ifBlank { null }, date))
    }, enabled = accountId > 0 && title.isNotBlank() && enteredAmount > 0 && !insufficient && (type != "transfer" || destinationId > 0)) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } })
}

@Composable
private fun GoalEditorDialog(current: SavingsGoal?, onDismiss: () -> Unit, onSave: (SavingsGoalRequest) -> Unit) {
    var title by remember(current) { mutableStateOf(current?.title.orEmpty()) }
    var target by remember(current) { mutableStateOf(current?.target_amount?.toString().orEmpty()) }
    var saved by remember(current) {
        mutableStateOf(current?.saved_amount?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var date by remember(current) { mutableStateOf(current?.target_date?.take(10).orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("هدف پس‌انداز") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppField(title, { title = it }, "عنوان هدف")
        AppField(target, { target = it.normalizeNumberDigits().filter(Char::isDigit) }, "مبلغ هدف (تومان)", KeyboardType.Number, visualTransformation = ThousandsSeparatorTransformation)
        AppField(saved, {
            val digits = it.normalizeNumberDigits().filter(Char::isDigit)
            saved = digits.trimStart('0').ifEmpty { if (digits.isEmpty()) "" else "0" }
        }, "مبلغ پس‌اندازشده", KeyboardType.Number, visualTransformation = ThousandsSeparatorTransformation)
        PersianDateField(date.ifBlank { null }, "تاریخ هدف (اختیاری)", optional = true, minDate = LocalDate.now()) { date = it.orEmpty() }
    } }, confirmButton = { Button(onClick = { onSave(SavingsGoalRequest(title, target.toLongOrNull() ?: 0, saved.toLongOrNull() ?: 0, date.ifBlank { null })) }, enabled = title.isNotBlank() && (target.toLongOrNull() ?: 0) > 0) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } })
}

@Composable fun ChoiceDropdown(label: String, selected: String, items: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(items.firstOrNull { it.first == selected }?.second ?: label) }
        DropdownMenu(expanded, { expanded = false }) { items.forEach { item -> DropdownMenuItem(text = { Text(item.second) }, onClick = { onSelect(item.first); expanded = false }) } }
    }
}

@Composable fun AccountDropdown(label: String, accounts: List<PersonalAccount>, selected: Long, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(accounts.firstOrNull { it.id == selected }?.let { "$label: ${it.name}" } ?: label) }
        DropdownMenu(expanded, { expanded = false }) { accounts.forEach { account -> DropdownMenuItem(text = { Text("${account.name} — ${account.balance.toTomanText()}") }, onClick = { onSelect(account.id); expanded = false }) } }
    }
}

private fun accountIcon(type: String) = when (type) { "cash" -> Icons.Default.Payments; "wallet" -> Icons.Default.AccountBalanceWallet; "bank" -> Icons.Default.AccountBalance; else -> Icons.Default.CreditCard }
private fun accountAccent(type: String) = when (type) {
    "cash" -> Color(0xFF07885A)
    "wallet" -> Color(0xFF7C3AED)
    "bank" -> Color(0xFF0E7490)
    "card" -> Color(0xFF1859D9)
    else -> Color(0xFFE66716)
}
private fun accountTypeText(type: String) = when (type) { "cash" -> "پول نقد"; "wallet" -> "کیف پول"; "bank" -> "حساب بانکی"; "card" -> "کارت بانکی"; else -> "سایر" }
