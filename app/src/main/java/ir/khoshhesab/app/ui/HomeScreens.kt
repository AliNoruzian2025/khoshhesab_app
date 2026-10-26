package ir.khoshhesab.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import ir.khoshhesab.app.AppViewModel
import ir.khoshhesab.app.R
import ir.khoshhesab.app.data.Group
import ir.khoshhesab.app.data.CreateGroupRequest
import java.time.LocalDate

@Composable
fun HomeScreen(
    vm: AppViewModel,
    openGroup: (Long) -> Unit,
    createGroup: () -> Unit,
    notifications: () -> Unit,
    chats: () -> Unit,
    personalFinance: () -> Unit,
    profile: () -> Unit,
    logout: () -> Unit
) {
    SystemBarAppearance(darkBackground = true)
    val context = LocalContext.current
    val appearancePrefs = remember { context.getSharedPreferences("appearance_preferences", android.content.Context.MODE_PRIVATE) }
    var customHomeBackground by remember { mutableStateOf(appearancePrefs.getString("home_background", null)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                customHomeBackground = appearancePrefs.getString("home_background", null)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var joinDialog by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    var officialDate by remember { mutableStateOf(todayPersianText()) }

    LaunchedEffect(refresh) {
        vm.request({ vm.api.groups(includeInactive = true) }) { groups = it }
        vm.request({ vm.api.notifications() }, silent = true) { unreadNotifications = it.unread_count }
        vm.request({ vm.api.serverTime() }, silent = true) { officialDate = persianDateWithWeekday(it.date) }
    }

    Box(Modifier.fillMaxSize()) {
        if (customHomeBackground != null) {
            AsyncImage(
                model = customHomeBackground,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.home_background_vault_duck),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xB30A1930),
                        Color(0x730A1930),
                        Color(0x8C061225)
                    )
                )
            )
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("خوش حساب", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("گروه‌ها و دنگ‌های من", color = Color.White.copy(alpha = .82f))
                    Surface(
                        color = Color.White.copy(alpha = .88f),
                        contentColor = Color(0xFF101828),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Today, null, tint = Color(0xFF123B8F), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(officialDate, color = Color(0xFF101828), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 0.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = notifications) {
                        BadgedBox(badge = {
                            if (unreadNotifications > 0) Badge {
                                Text(if (unreadNotifications > 99) "+99" else unreadNotifications.toString())
                            }
                        }) { Icon(Icons.Default.Notifications, "اعلان‌ها", tint = Color.White) }
                    }
                    IconButton(onClick = profile) { Icon(Icons.Default.Person, "پروفایل", tint = Color.White) }
                    IconButton(onClick = {
                        vm.request({ vm.api.logout() }) { logout() }
                    }) { Icon(Icons.Default.Logout, "خروج", tint = Color.White) }
                }
            }
            }
        ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable(onClick = personalFinance),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xEE073C35),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFFFD46A)) {
                        Icon(
                            Icons.Default.AccountBalanceWallet, null,
                            tint = Color(0xFF073C35), modifier = Modifier.padding(11.dp).size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("مدیریت مالی شخصی", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("حساب‌ها، درآمدها، هزینه‌ها و پس‌انداز", color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronLeft, null)
                }
            }
            OutlinedButton(
                onClick = { joinDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = .92f),
                    contentColor = Color(0xFF123B8F)
                )
            ) {
                Icon(Icons.Default.GroupAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("پیوستن با کد دعوت")
            }
            if (groups.isEmpty() && !vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = Color(0xC9142238),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "هنوز گروهی ندارید؛ اولین گروه را بسازید.",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(groups, key = { it.id }) { group ->
                        val isDang = group.type == "dang"
                        val cardColor = if (isDang) Color(0xFFE2ECFF) else Color(0xFFFFE9D5)
                        val accentColor = if (isDang) Color(0xFF1859D9) else Color(0xFFE66716)
                        ElevatedCard(
                            Modifier.fillMaxWidth().clickable { openGroup(group.id) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = cardColor,
                                contentColor = Color(0xFF111827)
                            )
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(58.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = .72f),
                                    shadowElevation = 3.dp
                                ) {
                                    if (!group.avatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = mediaUrl(group.avatar),
                                            contentDescription = group.name,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isDang) Icons.Default.Luggage else Icons.Default.AccountBalance,
                                            null, tint = accentColor,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(if (isDang) "دنگ کوتاه‌مدت" else "حساب بلندمدت", color = accentColor)
                                    if (!isDang && group.due_date != null) {
                                        Text("سررسید: ${group.due_date.toPersianDateText()}", fontSize = 13.sp)
                                    }
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(if (group.is_active) "فعال" else "غیرفعال", fontWeight = FontWeight.Bold) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (group.is_active) Color(0xFFD7F7EA) else Color(0xFFFFDDE1),
                                        labelColor = if (group.is_active) Color(0xFF056647) else Color(0xFF9B1C31)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }
        }
            ExtendedFloatingActionButton(
                onClick = chats,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                icon = {
                    val unreadChats = groups.sumOf { it.unread_count }
                    BadgedBox(badge = {
                        if (unreadChats > 0) Badge(containerColor = Color(0xFFD32F2F)) {
                            Text(if (unreadChats > 99) "+99" else unreadChats.toString())
                        }
                    }) { Icon(Icons.Default.Forum, null) }
                },
                text = { Text("چت‌ها") },
                containerColor = Color(0xFF00A17A),
                contentColor = Color.White
            )
            ExtendedFloatingActionButton(
                onClick = createGroup,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("گروه جدید") },
                containerColor = Color(0xFF1859D9),
                contentColor = Color.White
            )
            }
        }
    }

    if (joinDialog) {
        AlertDialog(
            onDismissRequest = { joinDialog = false },
            title = { Text("کد دعوت") },
            text = { AppField(inviteCode, { inviteCode = it.uppercase().take(10) }, "کد گروه") },
            confirmButton = {
                TextButton(onClick = {
                    vm.request({ vm.api.joinGroup(mapOf("invite_code" to inviteCode)) }) {
                        vm.showMessage(it.message)
                        joinDialog = false
                        refresh++
                    }
                }) { Text("پیوستن") }
            },
            dismissButton = { TextButton(onClick = { joinDialog = false }) { Text("انصراف") } }
        )
    }
}

@Composable
fun CreateGroupScreen(vm: AppViewModel, onBack: () -> Unit, onCreated: (Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("dang") }
    var dueDate by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf("") }
    var approval by remember { mutableStateOf(true) }
    var avatar by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PageHeader("گروه جدید", onBack)
        AppField(name, { name = it }, "نام گروه (فارسی یا انگلیسی)")
        ReceiptPicker(
            vm = vm,
            value = avatar.ifBlank { null },
            onUploaded = { avatar = it },
            groupImage = true
        )
        Text("نوع گروه", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                type == "dang", { type = "dang" }, { Text("دنگ") },
                leadingIcon = { Icon(Icons.Default.Luggage, null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDCE7FF), selectedLabelColor = Color(0xFF1859D9)
                )
            )
            FilterChip(
                type == "account", { type = "account" }, { Text("حساب") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFE2C6), selectedLabelColor = Color(0xFFE66716)
                )
            )
        }
        if (type == "account") {
            val startDate = LocalDate.now()
            val parsedDueDate = dueDate.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val parsedReminderDate = reminderDate.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            PersianDateField(
                dueDate.ifBlank { null },
                "تاریخ سررسید حساب",
                minDate = startDate
            ) {
                dueDate = it.orEmpty()
                if (parsedReminderDate != null && it != null && !parsedReminderDate.isBefore(LocalDate.parse(it))) {
                    reminderDate = ""
                }
            }
            PersianDateField(
                reminderDate.ifBlank { null },
                "تاریخ یادآوری (اختیاری)",
                optional = true,
                minDate = LocalDate.now(),
                maxDate = parsedDueDate?.minusDays(1),
                enabled = parsedDueDate != null
            ) {
                reminderDate = it.orEmpty()
            }
            Text(
                if (parsedDueDate == null) "ابتدا تاریخ سررسید را انتخاب کنید."
                else "تاریخ یادآوری باید قبل از تاریخ سررسید باشد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(approval, { approval = it })
            Spacer(Modifier.width(10.dp))
            Text("عضویت نیازمند تأیید سازنده باشد")
        }
        PrimaryButton("ایجاد گروه", {
            vm.request({
                vm.api.createGroup(CreateGroupRequest(
                    name = name,
                    type = type,
                    due_date = dueDate.ifBlank { null },
                    reminder_date = reminderDate.ifBlank { null },
                    avatar = avatar.ifBlank { null },
                    require_approval = approval
                ))
            }) {
                vm.showSuccess("گروه با موفقیت ساخته شد")
                onCreated(it.id)
            }
        }, name.isNotBlank() && (type == "dang" || (
            dueDate.isNotBlank() && !LocalDate.parse(dueDate).isBefore(LocalDate.now()) &&
                (reminderDate.isBlank() || LocalDate.parse(reminderDate).isBefore(LocalDate.parse(dueDate)))
            )))
    }
}
