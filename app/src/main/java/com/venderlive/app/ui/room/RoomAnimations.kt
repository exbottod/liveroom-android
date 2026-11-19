package com.venderlive.app.ui.room

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.ui.theme.BrandAccent
import com.venderlive.app.ui.theme.BrandPrimary

/**
 * Room motion effects, drawn from primitives so every value stays editable here and no
 * animation files are needed. These replace the .svga slots of the reference app: the
 * wording, colours and timing are ours.
 */

/** Three arcs that ripple outward from a speaking seat. */
@Composable
fun SpeakingWave(
    modifier: Modifier = Modifier,
    color: Color = BrandAccent,
    active: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "speaking")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    Canvas(modifier = modifier) {
        if (!active) return@Canvas
        val stroke = Stroke(width = size.minDimension * 0.06f, cap = StrokeCap.Round)
        repeat(3) { i ->
            val p = (phase + i / 3f) % 1f
            val inset = size.minDimension * (0.12f + 0.3f * p)
            val alpha = (1f - p) * 0.9f
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = 300f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = stroke,
            )
        }
    }
}

/** Gift combo counter that pops in and pulses while a combo keeps running. */
@Composable
fun GiftComboOverlay(
    combo: Int,
    modifier: Modifier = Modifier,
) {
    if (combo <= 0) return
    val transition = rememberInfiniteTransition(label = "combo")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceGlass)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .rotate(-6f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "COMBO",
            style = TextStyle(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFFD166), Color(0xFFFF4D8D))),
                fontSize = 18.sp * pulse,
                fontWeight = FontWeight.Black,
            ),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "x$combo",
            color = Color.White,
            fontSize = 18.sp * pulse,
            fontWeight = FontWeight.Black,
        )
    }
}

/** Banner shown while two users are bound as a couple. */
@Composable
fun CpBoundBanner(
    leftName: String,
    rightName: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "cp")
    val glow by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow",
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        BrandAccent.copy(alpha = 0.35f * glow),
                        BrandPrimary.copy(alpha = 0.35f * glow),
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        HeartDot()
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$leftName  CP TERIKAT  $rightName",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(8.dp))
        HeartDot()
    }
}

@Composable
private fun HeartDot() {
    Canvas(modifier = Modifier.size(12.dp)) {
        val r = size.minDimension / 2
        drawCircle(Color(0xFFFF6B9D), radius = r * 0.42f, center = Offset(r * 0.72f, r * 0.9f))
        drawCircle(Color(0xFFFF6B9D), radius = r * 0.42f, center = Offset(r * 1.28f, r * 0.9f))
        drawCircle(Color(0xFFFF6B9D), radius = r * 0.72f, center = Offset(r, r * 1.15f))
    }
}

/** Slowly rotating disc used by the music tab. */
@Composable
fun MusicDisc(
    modifier: Modifier = Modifier,
    playing: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "disc")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "angle",
    )
    Canvas(modifier = modifier.rotate(if (playing) angle else 0f)) {
        val r = size.minDimension / 2
        drawCircle(Color(0xFF1A1526), radius = r)
        drawCircle(Color(0xFF2C2340), radius = r * 0.92f, style = Stroke(width = r * 0.04f))
        drawCircle(Color(0xFF2C2340), radius = r * 0.66f, style = Stroke(width = r * 0.03f))
        drawCircle(
            brush = Brush.radialGradient(listOf(BrandAccent, BrandPrimary)),
            radius = r * 0.34f,
        )
        drawCircle(Color(0xFF0E0B16), radius = r * 0.08f)
    }
}

/** Glassy pill colour shared by the overlays. */
private val SurfaceGlass = Color(0xE01A1526)

/** Circular badge wrapper so the overlays line up with the room chrome. */
@Composable
fun RoundChip(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xE0201630)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
