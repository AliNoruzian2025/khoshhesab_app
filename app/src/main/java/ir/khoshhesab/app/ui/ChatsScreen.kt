package ir.khoshhesab.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import ir.khoshhesab.app.AppViewModel
import ir.khoshhesab.app.data.Group

@Composable
fun ChatsScreen(vm: AppViewModel, onBack: () -> Unit, openChat: (Long) -> Unit) {
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    val context = LocalContext.current
    val chatPrefs = remember { context.getSharedPreferences("chat_preferences", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        vm.request({ vm.api.groups(includeInactive = true) }) { groups = it }
    }

    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .38f), MaterialTheme.colorScheme.background)
            )
        )
    ) {
        PageHeader("گفت‌وگوها", onBack)
        if (groups.isEmpty() && !vm.loading) {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.MarkChatUnread, null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(14.dp))
                Text("شما هنوز دنگ یا حسابی ندارید", fontWeight = FontWeight.Bold)
                Text(
                    "بعد از ساختن یا عضو شدن در یک دنگ یا حساب، گفت‌وگوی آن اینجا نمایش داده می‌شود.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                            Icon(Icons.Default.Forum, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(11.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("پیام‌های دنگ‌ها و حساب‌ها", fontWeight = FontWeight.Black)
                            Text("آخرین گفت‌وگوها همیشه اینجا در دسترس‌اند", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            items(groups, key = { it.id }) { group ->
                val isDang = group.type == "dang"
                val accent = if (isDang) Color(0xFF1859D9) else Color(0xFFE66716)
                val last = group.latest_message
                val localName = chatPrefs.getString("name_${group.id}", null).orEmpty().ifBlank { group.name }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().clickable { openChat(group.id) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (group.unread_count > 0) 8.dp else 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (group.unread_count > 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = accent.copy(alpha = .14f)
                        ) {
                            if (!group.avatar.isNullOrBlank()) AsyncImage(
                                model = mediaUrl(group.avatar),
                                contentDescription = localName,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            ) else Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Forum, null, tint = accent)
                            }
                        }
                    },
                    headlineContent = {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(localName, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1)
                            last?.created_at?.let {
                                Text(it.toTehranTimeText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    supportingContent = {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (isDang) "دنگ" else "حساب", color = accent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                Text(if (group.is_active) "فعال" else "بایگانی‌شده", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                last?.let(::chatPreview) ?: "هنوز پیامی ارسال نشده است؛ اولین پیام را بفرستید",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingContent = {
                        if (group.unread_count > 0) Badge(containerColor = Color(0xFF00A17A)) {
                            Text(if (group.unread_count > 99) "+99" else group.unread_count.toString())
                        }
                    }
                )
                }
            }
        }
    }
}

private fun chatPreview(message: ir.khoshhesab.app.data.ChatMessage): String = when {
    message.deleted_at != null -> "پیام حذف شده"
    message.is_system -> message.message.orEmpty()
    !message.receipt_image.isNullOrBlank() -> "📷 تصویر"
    !message.sticker.isNullOrBlank() -> message.sticker
    else -> message.message.orEmpty()
}
