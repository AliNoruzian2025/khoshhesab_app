package ir.khoshhesab.app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.CircleShape
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SystemBarAppearance(darkBackground: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, darkBackground) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = !darkBackground
        controller?.isAppearanceLightNavigationBars = !darkBackground
        onDispose {
            if (previousStatus != null) controller?.isAppearanceLightStatusBars = previousStatus
            if (previousNavigation != null) controller?.isAppearanceLightNavigationBars = previousNavigation
        }
    }
}

@Composable
fun PageHeader(title: String, onBack: (() -> Unit)? = null) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        tonalElevation = 6.dp,
        shadowElevation = 5.dp
    ) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .9f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontSize = 16.sp) }
}

fun String.normalizeNumberDigits(): String = map { char ->
    when (char) {
        in '۰'..'۹' -> ('0'.code + (char.code - '۰'.code)).toChar()
        in '٠'..'٩' -> ('0'.code + (char.code - '٠'.code)).toChar()
        else -> char
    }
}.joinToString("")

fun Long.toTomanText(): String = "%,d تومان".format(this)

fun String.toTehranTimeText(): String {
    val tehran = ZoneId.of("Asia/Tehran")
    val zoned = runCatching { Instant.parse(this).atZone(tehran) }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(tehran)
        }.getOrNull()
    return zoned?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: take(16).takeLast(5)
}

object ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter(Char::isDigit)
        val formatted = raw.reversed().chunked(3).joinToString(",").reversed()
        val originalToTransformed = IntArray(raw.length + 1)
        var rawIndex = 0
        originalToTransformed[0] = 0
        formatted.forEachIndexed { index, char ->
            if (char.isDigit()) {
                rawIndex++
                originalToTransformed[rawIndex] = index + 1
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
