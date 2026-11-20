package com.venderlive.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MomentPost(
    val id: String,
    val author: String,
    val level: Int,
    val isNew: Boolean,
    val text: String,
    val photos: Int,
    val timeAgo: String,
    val gifts: Int = 0,
    val comments: Int = 0,
    val likes: Int = 0,
    val tint: Color = Color(0xFFBDB6AE),
    val following: Boolean = false,
)

/** Real posts come from the server; until then the tab is empty. */
val NoMoments: List<MomentPost> = emptyList()

private val MomentTeal = Color(0xFF1CD3B2)
private val Ink = Color(0xFF22222B)
private val Soft = Color(0xFF9A9AA6)
private val MomentChips = listOf("Populer", "Baru", "Ikuti")

@Composable
fun MomentScreen(
    posts: List<MomentPost> = NoMoments,
    onCompose: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    var chip by remember { mutableIntStateOf(0) }
    var followed by remember { androidx.compose.runtime.mutableStateOf(emptySet<String>()) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Momen", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                BellGlyph(Modifier.size(28.dp).clickable(onClick = onNotifications))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MomentChips.forEachIndexed { i, label ->
                    val on = i == chip
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                            .background(if (on) MomentTeal else Color(0xFFF3F3F5))
                            .clickable { chip = i }
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(chipDot(i)))
                        Spacer(Modifier.width(6.dp))
                        Text(label, color = if (on) Color.White else Ink, fontSize = 16.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)) {
                if (posts.isEmpty()) {
                    item { EmptyState("Belum ada momen", "Momen dari teman dan orang yang kamu ikuti akan muncul di sini.") }
                }
                items(posts, key = { it.id }) { p ->
                    PostCard(p, isFollowing = p.id in followed) {
                        followed = if (p.id in followed) followed - p.id else followed + p.id
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F3)))
                }
            }
        }
        ComposeFab(onCompose, Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 26.dp))
    }
}

private fun chipDot(i: Int) = when (i) {
    0 -> Color(0xFFFFB347)
    1 -> Color(0xFFFFD54F)
    else -> Color(0xFFFFC857)
}

@Composable
private fun PostCard(p: MomentPost, isFollowing: Boolean, onFollow: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(p.tint, p.tint.copy(alpha = 0.6f)))),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(p.author, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelPill(p.level)
                    if (p.isNew) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF3AA8FF), Color(0xFF1B7FE6))))
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                        ) { Text("NEW", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    }
                }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50))
                    .background(if (isFollowing) Color(0xFFE6E6EA) else MomentTeal)
                    .clickable(onClick = onFollow)
                    .padding(horizontal = 22.dp, vertical = 6.dp),
            ) { Text(if (isFollowing) "Diikuti" else "+ Ikuti", color = if (isFollowing) Soft else Color.White, fontSize = 13.sp) }
            Spacer(Modifier.width(10.dp))
            MoreDots()
        }
        Column(modifier = Modifier.padding(start = 56.dp, top = 6.dp)) {
            if (p.text.isNotEmpty()) {
                Text(p.text, color = Ink, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
            }
            PhotoStrip(p)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.timeAgo, color = Soft, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Counter(p.gifts) { GiftGlyph(Modifier.size(24.dp)) }
                Spacer(Modifier.width(18.dp))
                Counter(p.comments) { CommentGlyph(Modifier.size(24.dp)) }
                Spacer(Modifier.width(18.dp))
                Counter(p.likes) { HeartGlyph(Modifier.size(24.dp)) }
            }
        }
    }
}

@Composable
private fun PhotoStrip(p: MomentPost) {
    if (p.photos <= 1) {
        Box(
            modifier = Modifier.fillMaxWidth(0.62f).aspectRatio(0.62f).clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(p.tint, p.tint.copy(alpha = 0.7f)))),
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
            repeat(p.photos.coerceAtMost(3)) { i ->
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(listOf(p.tint.copy(alpha = 1f - i * 0.12f), p.tint.copy(alpha = 0.6f)))),
                )
            }
        }
    }
}

@Composable
private fun Counter(count: Int, icon: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$count", color = Ink, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        icon()
    }
}

@Composable
private fun LevelPill(level: Int) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(Color(0xFFB9BCC4), Color(0xFFE3E5EA))))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(0xFF6B6F7A)))
        Spacer(Modifier.width(4.dp))
        Text("Lv.$level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MoreDots() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(top = 4.dp)) {
        repeat(3) { Box(Modifier.size(3.dp).clip(CircleShape).background(Soft)) }
    }
}

@Composable
private fun ComposeFab(onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.size(60.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF52E3C6), Color(0xFF1CC6B0))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { PencilGlyph(Modifier.size(28.dp)) }
}

@Composable
private fun PencilGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val body = Path().apply {
            moveTo(w * 0.62f, h * 0.12f); lineTo(w * 0.88f, h * 0.38f); lineTo(w * 0.34f, h * 0.84f)
            lineTo(w * 0.1f, h * 0.88f); lineTo(w * 0.14f, h * 0.64f); close()
        }
        drawPath(body, Color.White)
        drawLine(Color(0xFF1CC6B0), Offset(w * 0.56f, h * 0.2f), Offset(w * 0.8f, h * 0.44f), strokeWidth = w * 0.05f)
        drawLine(Color.White, Offset(w * 0.46f, h * 0.94f), Offset(w * 0.92f, h * 0.94f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    }
}

@Composable
private fun BellGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val bell = Path().apply {
            moveTo(w * 0.2f, h * 0.72f); cubicTo(w * 0.3f, h * 0.6f, w * 0.26f, h * 0.4f, w * 0.3f, h * 0.34f)
            cubicTo(w * 0.36f, h * 0.14f, w * 0.64f, h * 0.14f, w * 0.7f, h * 0.34f)
            cubicTo(w * 0.74f, h * 0.4f, w * 0.7f, h * 0.6f, w * 0.8f, h * 0.72f); close()
        }
        drawPath(bell, Ink, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawCircle(Ink, radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.84f))
    }
}

@Composable
private fun GiftGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(Color(0xFFFF8A1F), Offset(w * 0.1f, h * 0.38f), androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.5f), androidx.compose.ui.geometry.CornerRadius(6f))
        drawRoundRect(Color(0xFFFFA84D), Offset(w * 0.06f, h * 0.26f), androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.18f), androidx.compose.ui.geometry.CornerRadius(6f))
        drawRect(Color(0xFFFFE08A), Offset(w * 0.46f, h * 0.26f), androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.62f))
        drawCircle(Color(0xFFFF8A1F), radius = w * 0.1f, center = Offset(w * 0.38f, h * 0.2f), style = Stroke(width = w * 0.05f))
        drawCircle(Color(0xFFFF8A1F), radius = w * 0.1f, center = Offset(w * 0.62f, h * 0.2f), style = Stroke(width = w * 0.05f))
    }
}

@Composable
private fun CommentGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(Ink, Offset(w * 0.08f, h * 0.12f), androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.62f),
            androidx.compose.ui.geometry.CornerRadius(w * 0.2f), style = Stroke(width = w * 0.07f))
        drawLine(Ink, Offset(w * 0.3f, h * 0.34f), Offset(w * 0.7f, h * 0.34f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
        drawLine(Ink, Offset(w * 0.3f, h * 0.5f), Offset(w * 0.56f, h * 0.5f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
        val tail = Path().apply { moveTo(w * 0.34f, h * 0.74f); lineTo(w * 0.3f, h * 0.92f); lineTo(w * 0.52f, h * 0.74f) }
        drawPath(tail, Ink, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
    }
}

@Composable
private fun HeartGlyph(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val heart = Path().apply {
            moveTo(w * 0.5f, h * 0.86f)
            cubicTo(w * 0.02f, h * 0.56f, w * 0.1f, h * 0.12f, w * 0.34f, h * 0.18f)
            cubicTo(w * 0.44f, h * 0.2f, w * 0.5f, h * 0.3f, w * 0.5f, h * 0.3f)
            cubicTo(w * 0.5f, h * 0.3f, w * 0.56f, h * 0.2f, w * 0.66f, h * 0.18f)
            cubicTo(w * 0.9f, h * 0.12f, w * 0.98f, h * 0.56f, w * 0.5f, h * 0.86f); close()
        }
        drawPath(heart, Ink, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
    }
}
