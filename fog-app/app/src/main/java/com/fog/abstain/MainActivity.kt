package com.fog.abstain

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Bg = Color(0xFF0A0C0B)
private val Pale = Color(0xFFC9D0CA)
private val Dim = Color(0xFF7C867F)

private val Lines = listOf(
    "Туман скрывает лишнее. Иди дальше.",
    "Тишина — тоже ответ.",
    "Ещё один шаг сквозь белую мглу.",
    "Не торопись. Просто не останавливайся.",
    "Здесь никого нет. Только ты и выбор.",
    "Слабость приходит и уходит, как помехи в радио.",
    "Не оглядывайся. Позади только дым."
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val p = getSharedPreferences("fog", MODE_PRIVATE)
        setContent { FogApp(p) }
    }
}

@Composable
fun FogApp(p: SharedPreferences) {
    var start by remember { mutableLongStateOf(p.getLong("start", 0L)) }
    var best by remember { mutableLongStateOf(p.getLong("best", 0L)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirm by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }

    val cur = if (start == 0L) 0L else (now - start).coerceAtLeast(0L)
    val sec = cur / 1000
    val days = sec / 86400
    val hms = "%02d:%02d:%02d".format((sec / 3600) % 24, (sec / 60) % 60, sec % 60)
    val bestDays = maxOf(best, cur) / 86400000

    Box(Modifier.fillMaxSize().background(Bg)) {
        Fog()
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ТУМАН", color = Dim, fontSize = 12.sp, letterSpacing = 6.sp, fontFamily = FontFamily.Serif)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (start == 0L) {
                    Text("Путь не начат", color = Pale, fontSize = 26.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(24.dp))
                    TextButton(onClick = {
                        start = System.currentTimeMillis()
                        p.edit().putLong("start", start).apply()
                    }) { Text("НАЧАТЬ", color = Pale, letterSpacing = 4.sp) }
                } else {
                    Text("ПРОШЛО ДНЕЙ", color = Dim, fontSize = 12.sp, letterSpacing = 5.sp)
                    Text("$days", color = Pale, fontSize = 112.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraLight)
                    Text(hms, color = Dim, fontSize = 18.sp, fontFamily = FontFamily.Monospace, letterSpacing = 3.sp)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (start != 0L) {
                    Text(
                        Lines[(days % Lines.size).toInt()], color = Dim, fontSize = 15.sp,
                        fontFamily = FontFamily.Serif, textAlign = TextAlign.Center, lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text("РЕКОРД · $bestDays", color = Dim, fontSize = 11.sp, letterSpacing = 4.sp)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { confirm = true }) {
                        Text("СОРВАЛСЯ", color = Color(0xFF8A5A55), fontSize = 12.sp, letterSpacing = 4.sp)
                    }
                }
            }
        }
    }

    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        containerColor = Color(0xFF111413),
        title = { Text("Начать заново?", color = Pale, fontFamily = FontFamily.Serif) },
        text = { Text("Счётчик обнулится. Рекорд сохранится.", color = Dim) },
        confirmButton = {
            TextButton(onClick = {
                best = maxOf(best, cur)
                start = System.currentTimeMillis()
                p.edit().putLong("best", best).putLong("start", start).apply()
                confirm = false
            }) { Text("Да", color = Pale) }
        },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Нет", color = Dim) } }
    )
}

@Composable
fun Fog() {
    val t = rememberInfiniteTransition(label = "fog")
    val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val b by t.animateFloat(1f, 0f, infiniteRepeatable(tween(34000, easing = LinearEasing), RepeatMode.Reverse), label = "b")
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        fun blob(cx: Float, cy: Float, r: Float, c: Color) =
            drawCircle(Brush.radialGradient(listOf(c, Color.Transparent), Offset(cx, cy), r), r, Offset(cx, cy))
        blob(w * (0.2f + 0.6f * a), h * (0.25f + 0.10f * b), w * 0.9f, Color(0x33B9C2BC))
        blob(w * (0.8f - 0.6f * b), h * (0.70f + 0.10f * a), w * 1.0f, Color(0x2A9AA59E))
        blob(w * 0.5f, h * (0.40f + 0.30f * a), w * 0.7f, Color(0x1FD0D6D0))
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0xDD000000)), Offset(w / 2, h / 2), h * 0.75f))
    }
}
