package com.venderlive.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** What one reward cell shows. Add kinds here when new prizes appear. */
enum class RewardKind { COIN, FRAME, SCOOTER, BACKDROP, BADGE, EXP, CAR }

data class DayReward(val kind: RewardKind, val label: String)

data class CheckInDay(val day: Int, val rewards: List<DayReward>)

val SampleCheckInDays = listOf(
    CheckInDay(1, listOf(DayReward(RewardKind.COIN, "X20,000"))),
    CheckInDay(2, listOf(DayReward(RewardKind.COIN, "X20,000"))),
    CheckInDay(3, listOf(DayReward(RewardKind.FRAME, "1 hari"))),
    CheckInDay(4, listOf(DayReward(RewardKind.COIN, "X40,000"))),
    CheckInDay(5, listOf(DayReward(RewardKind.COIN, "X40,000"), DayReward(RewardKind.SCOOTER, "1 hari"))),
    CheckInDay(6, listOf(DayReward(RewardKind.COIN, "X40,000"), DayReward(RewardKind.BACKDROP, "3 hari"))),
    CheckInDay(
        7,
        listOf(
            DayReward(RewardKind.COIN, "X60,000"),
            DayReward(RewardKind.BADGE, "3 hari"),
            DayReward(RewardKind.CAR, "3 hari"),
            DayReward(RewardKind.EXP, "X5,000"),
        ),
    ),
)

private val DialogTop = Color(0xFF2BC9E8)
private val DialogBottom = Color(0xFF22D6B0)
private val CellBg = Color(0xFFE2F8F6)
private val CellFoot = Color(0xFF1FC4BC)
private val ButtonBrush = Brush.horizontalGradient(listOf(Color(0xFF1FD8B0), Color(0xFF1CC9C5)))

@Composable
fun DailyCheckInDialog(
    days: List<CheckInDay> = SampleCheckInDays,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.padding(horizontal = 26.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(DialogTop, DialogBottom)))
                    .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 14.dp),
            ) {
                Text("Hadiah Harian", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Login 7 hari, dapat hadiah besar",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFF4FFFD))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        days.take(4).forEach { DayCell(it, Modifier.weight(1f)) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        days.drop(4).take(2).forEach { DayCell(it, Modifier.weight(1f)) }
                    }
                    days.drop(6).firstOrNull()?.let { DayCell(it, Modifier.fillMaxWidth()) }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(ButtonBrush)
                            .clickable(onClick = onClaim),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Masuk Sekarang", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            CalendarBadge(Modifier.align(Alignment.TopEnd).padding(end = 6.dp).size(92.dp))
        }
    }
}

@Composable
private fun DayCell(day: CheckInDay, modifier: Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(CellBg)) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 0.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                    .background(CellFoot)
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) { Text("${day.day}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                day.rewards.forEach { RewardIcon(it.kind, Modifier.size(if (day.rewards.size > 2) 44.dp else 48.dp)) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(CellFoot).padding(vertical = 3.dp)) {
            day.rewards.forEach {
                Text(
                    it.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Every prize icon is drawn from shapes, so no bitmap is needed and colours are editable here. */
@Composable
fun RewardIcon(kind: RewardKind, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (kind) {
            RewardKind.COIN -> {
                drawCircle(Brush.linearGradient(listOf(Color(0xFFFFE27A), Color(0xFFF5A623))), radius = w * 0.46f)
                drawCircle(Color(0xFFE8931A), radius = w * 0.36f, style = Stroke(width = w * 0.05f))
                val star = Path().apply {
                    val cx = w / 2; val cy = h / 2; val ro = w * 0.2f; val ri = w * 0.09f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) ro else ri
                        val a = Math.toRadians((i * 36 - 90).toDouble())
                        val x = cx + (r * Math.cos(a)).toFloat(); val y = cy + (r * Math.sin(a)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(star, Color(0xFFFFF3C4))
            }
            RewardKind.FRAME -> {
                drawCircle(Color(0xFFBFE9D2), radius = w * 0.4f, style = Stroke(width = w * 0.07f))
                drawCircle(Color(0xFFFFB3C7), radius = w * 0.07f, center = Offset(w * 0.3f, h * 0.22f))
                drawCircle(Color(0xFFFFE08A), radius = w * 0.06f, center = Offset(w * 0.78f, h * 0.7f))
            }
            RewardKind.SCOOTER -> {
                drawCircle(Color(0xFF3A3A44), radius = w * 0.12f, center = Offset(w * 0.24f, h * 0.76f))
                drawCircle(Color(0xFF3A3A44), radius = w * 0.12f, center = Offset(w * 0.78f, h * 0.76f))
                drawRoundRect(Color(0xFFE9EDF2), Offset(w * 0.2f, h * 0.42f), Size(w * 0.5f, h * 0.26f), CornerRadius(w * 0.1f))
                drawLine(Color(0xFF3A3A44), Offset(w * 0.66f, h * 0.44f), Offset(w * 0.78f, h * 0.2f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
            }
            RewardKind.BACKDROP -> {
                drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF2B4C7E), Color(0xFF0E1A33))), Offset(w * 0.1f, h * 0.1f), Size(w * 0.8f, h * 0.8f), CornerRadius(6f))
                val peak = Path().apply { moveTo(w * 0.1f, h * 0.9f); lineTo(w * 0.45f, h * 0.35f); lineTo(w * 0.6f, h * 0.6f); lineTo(w * 0.72f, h * 0.45f); lineTo(w * 0.9f, h * 0.9f); close() }
                drawPath(peak, Color(0xFFDCE8F5))
            }
            RewardKind.BADGE -> {
                drawCircle(Color(0xFF7FD6E8), radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.55f))
                drawCircle(Color(0xFFFFE08A), radius = w * 0.16f, center = Offset(w * 0.32f, h * 0.3f))
                drawArc(Color(0xFF2E9E6B), 200f, 140f, false, Offset(w * 0.1f, h * 0.55f), Size(w * 0.8f, h * 0.3f), style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
            }
            RewardKind.CAR -> {
                drawRoundRect(Color(0xFFF2B233), Offset(w * 0.12f, h * 0.38f), Size(w * 0.76f, h * 0.3f), CornerRadius(w * 0.08f))
                drawRoundRect(Color(0xFF5B4B2B), Offset(w * 0.26f, h * 0.2f), Size(w * 0.48f, h * 0.2f), CornerRadius(w * 0.06f))
                drawCircle(Color(0xFF2A2A30), radius = w * 0.1f, center = Offset(w * 0.28f, h * 0.7f))
                drawCircle(Color(0xFF2A2A30), radius = w * 0.1f, center = Offset(w * 0.72f, h * 0.7f))
            }
            RewardKind.EXP -> {
                drawCircle(Brush.linearGradient(listOf(Color(0xFFBFD7FF), Color(0xFF7DA2E8))), radius = w * 0.42f)
                drawCircle(Color.White.copy(alpha = 0.7f), radius = w * 0.36f, style = Stroke(width = w * 0.03f))
            }
        }
    }
}

/** Tilted calendar with a check mark, top right of the dialog. */
@Composable
private fun CalendarBadge(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(Color(0xFFFFB3D1), Offset(w * 0.02f, h * 0.34f), Size(w * 0.34f, h * 0.34f), CornerRadius(10f))
        drawRoundRect(Color(0xFF9EE7F5), Offset(w * 0.68f, h * 0.38f), Size(w * 0.3f, h * 0.36f), CornerRadius(10f))
        drawRoundRect(Color.White, Offset(w * 0.18f, h * 0.12f), Size(w * 0.62f, h * 0.7f), CornerRadius(14f))
        drawRoundRect(Color(0xFF58C9E6), Offset(w * 0.18f, h * 0.12f), Size(w * 0.62f, h * 0.18f), CornerRadius(14f))
        drawCircle(Color(0xFF2B8FB5), radius = w * 0.03f, center = Offset(w * 0.34f, h * 0.12f))
        drawCircle(Color(0xFF2B8FB5), radius = w * 0.03f, center = Offset(w * 0.64f, h * 0.12f))
        val tick = Path().apply { moveTo(w * 0.32f, h * 0.55f); lineTo(w * 0.46f, h * 0.68f); lineTo(w * 0.7f, h * 0.4f) }
        drawPath(tick, Color(0xFF2B8FB5), style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
    }
}
