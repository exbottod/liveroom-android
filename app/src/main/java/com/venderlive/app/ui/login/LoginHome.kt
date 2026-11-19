package com.venderlive.app.ui.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.data.model.AuthResult

private val LoginTeal = Color(0xFF1FD3B3)
private val FbBlue = Color(0xFF4285F4)

/**
 * Landing login: faded photo collage, logo, Facebook, Google, small icons for other
 * methods, and the agreement line. Phone/OTP lives behind the phone icon.
 * Social buttons are wired to a callback so Firebase providers can be plugged in later.
 */
@Composable
fun LoginHome(
    viewModel: LoginViewModel,
    onLoggedIn: (AuthResult) -> Unit,
    onPhone: () -> Unit,
    onFacebook: () -> Unit = {},
    onGoogle: () -> Unit = {},
    onTelegram: () -> Unit = {},
    onGuest: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    val ctx = LocalContext.current
    fun soon(name: String) = Toast.makeText(ctx, "$name segera tersedia", Toast.LENGTH_SHORT).show()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        PhotoCollage(Modifier.fillMaxSize())
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.55f),
                    0.55f to Color.White.copy(alpha = 0.9f),
                    0.72f to Color.White,
                    1f to Color.White,
                ),
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(190.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppLogoMark(Modifier.size(78.dp))
                Spacer(Modifier.width(14.dp))
                Text(
                    "VenderLive",
                    color = LoginTeal,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(Modifier.weight(1f))

            SocialButton(
                label = "facebook",
                filled = true,
                onClick = { onFacebook(); soon("Login Facebook") },
                icon = { FacebookGlyph(Modifier.size(28.dp)) },
            )
            Spacer(Modifier.height(14.dp))
            SocialButton(
                label = "Google",
                filled = false,
                onClick = { onGoogle(); soon("Login Google") },
                icon = { GoogleGlyph(Modifier.size(26.dp)) },
            )

            Spacer(Modifier.height(64.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                SmallMethod(Color(0xFF3AA4F0), onClick = { onTelegram(); soon("Login Telegram") }) { TelegramGlyph(Modifier.size(20.dp)) }
                SmallMethod(LoginTeal, onClick = onPhone) { PhoneGlyph(Modifier.size(18.dp)) }
                SmallMethod(Color(0xFFE6E6EA), onClick = { onGuest(); soon("Login tamu") }) { GuestGlyph(Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            AgreementLine(onOpenTerms, onOpenPrivacy)
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun SocialButton(label: String, filled: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).clip(shape)
            .then(if (filled) Modifier.background(FbBlue) else Modifier.background(Color.White))
            .then(if (filled) Modifier else Modifier.androidxBorder())
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(label, color = if (filled) Color.White else Color(0xFF22222B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

private fun Modifier.androidxBorder(): Modifier =
    this.then(Modifier.border(BorderStroke(2.dp, Color(0xFF1B1B22)), RoundedCornerShape(50)))

@Composable
private fun SmallMethod(bg: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun AgreementLine(onTerms: () -> Unit, onPrivacy: () -> Unit) {
    val ink = Color(0xFF22222B)
    val link = Color(0xFF1FC3B0)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Login berarti setuju dengan  ", color = ink, fontSize = 14.sp)
            Text(
                "Perjanjian Pengguna",
                color = link,
                fontSize = 14.sp,
                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
                modifier = Modifier.clickable(onClick = onTerms),
            )
            Text("  dan", color = ink, fontSize = 14.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "Kebijakan Privasi",
            color = link,
            fontSize = 14.sp,
            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
            modifier = Modifier.clickable(onClick = onPrivacy),
        )
    }
}

/** Faded grid of soft tiles standing in for the photo collage. Swap for real photos later. */
@Composable
private fun PhotoCollage(modifier: Modifier) {
    val tones = listOf(
        Color(0xFFE9D6C8), Color(0xFFD7DCE8), Color(0xFFEBCFD6), Color(0xFFD4E4DA),
        Color(0xFFE6DDC9), Color(0xFFDAD2E8), Color(0xFFE9CFC0), Color(0xFFCFE0E8),
    )
    Canvas(modifier = modifier) {
        val cols = 4
        val gap = 8.dp.toPx()
        val tileW = (size.width - gap * (cols + 1)) / cols
        val tileH = tileW * 1.45f
        for (c in 0 until cols) {
            val offset = if (c % 2 == 0) 0f else -tileH * 0.45f
            var y = offset + gap
            var i = c
            while (y < size.height * 0.75f) {
                drawRoundRect(
                    tones[(i * 3 + c) % tones.size],
                    Offset(gap + c * (tileW + gap), y),
                    Size(tileW, tileH),
                    CornerRadius(14.dp.toPx()),
                )
                y += tileH + gap
                i++
            }
        }
    }
}

/** Our own logo: a rounded teal tile with a mic-and-headset glyph. Not the reference mascot. */
@Composable
fun AppLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(
            Brush.linearGradient(listOf(Color(0xFF39E0C4), Color(0xFF14B8A6))),
            Offset.Zero, Size(w, h), CornerRadius(w * 0.26f),
        )
        // microphone body
        drawRoundRect(Color.White, Offset(w * 0.4f, h * 0.2f), Size(w * 0.2f, h * 0.36f), CornerRadius(w * 0.1f))
        // holder arc
        drawArc(Color.White, 0f, 180f, false, Offset(w * 0.3f, h * 0.34f), Size(w * 0.4f, h * 0.3f),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))
        drawLine(Color.White, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.5f, h * 0.78f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(w * 0.38f, h * 0.78f), Offset(w * 0.62f, h * 0.78f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
        // sound dots
        drawCircle(Color(0xFFFFE08A), radius = w * 0.04f, center = Offset(w * 0.22f, h * 0.42f))
        drawCircle(Color(0xFFFFE08A), radius = w * 0.04f, center = Offset(w * 0.78f, h * 0.42f))
    }
}

@Composable
private fun FacebookGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color.White, radius = w / 2f)
        val f = Path().apply {
            moveTo(w * 0.56f, h * 0.9f); lineTo(w * 0.56f, h * 0.56f); lineTo(w * 0.68f, h * 0.56f)
            lineTo(w * 0.7f, h * 0.43f); lineTo(w * 0.56f, h * 0.43f); lineTo(w * 0.56f, h * 0.35f)
            cubicTo(w * 0.56f, h * 0.29f, w * 0.6f, h * 0.27f, w * 0.65f, h * 0.27f)
            lineTo(w * 0.71f, h * 0.27f); lineTo(w * 0.71f, h * 0.15f); lineTo(w * 0.62f, h * 0.15f)
            cubicTo(w * 0.47f, h * 0.15f, w * 0.42f, h * 0.25f, w * 0.42f, h * 0.35f)
            lineTo(w * 0.42f, h * 0.43f); lineTo(w * 0.31f, h * 0.43f); lineTo(w * 0.31f, h * 0.56f)
            lineTo(w * 0.42f, h * 0.56f); lineTo(w * 0.42f, h * 0.9f); close()
        }
        drawPath(f, FbBlue)
    }
}

@Composable
private fun GoogleGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val sw = w * 0.2f
        val inset = sw / 2f
        val box = Size(w - sw, w - sw)
        val tl = Offset(inset, inset)
        drawArc(Color(0xFFEA4335), 200f, 90f, false, tl, box, style = Stroke(width = sw))
        drawArc(Color(0xFFFBBC05), 140f, 60f, false, tl, box, style = Stroke(width = sw))
        drawArc(Color(0xFF34A853), 50f, 90f, false, tl, box, style = Stroke(width = sw))
        drawArc(Color(0xFF4285F4), -40f, 90f, false, tl, box, style = Stroke(width = sw))
        drawLine(Color(0xFF4285F4), Offset(w * 0.5f, w * 0.5f), Offset(w * 0.96f, w * 0.5f), strokeWidth = sw * 0.9f)
    }
}

@Composable
private fun TelegramGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val plane = Path().apply {
            moveTo(w * 0.94f, h * 0.1f); lineTo(w * 0.1f, h * 0.46f); lineTo(w * 0.34f, h * 0.56f)
            lineTo(w * 0.42f, h * 0.84f); lineTo(w * 0.56f, h * 0.66f); lineTo(w * 0.76f, h * 0.84f); close()
        }
        drawPath(plane, Color.White)
    }
}

@Composable
private fun PhoneGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(Color.White, Offset(w * 0.22f, h * 0.04f), Size(w * 0.56f, h * 0.92f), CornerRadius(w * 0.12f),
            style = Stroke(width = w * 0.1f))
        drawLine(Color.White, Offset(w * 0.42f, h * 0.82f), Offset(w * 0.58f, h * 0.82f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
    }
}

@Composable
private fun GuestGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color(0xFFA9A9B3), radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.34f))
        drawArc(Color(0xFFA9A9B3), 180f, 180f, true, Offset(w * 0.16f, h * 0.55f), Size(w * 0.68f, h * 0.7f))
    }
}
