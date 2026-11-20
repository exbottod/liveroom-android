package com.venderlive.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ConversationKind { OFFICIAL_TEAM, GIFT_NOTICE, USER }

data class Conversation(
    val id: String,
    val kind: ConversationKind,
    val title: String,
    val preview: String = "",
    val time: String = "",
    val unread: Int = 0,
)

/** Real conversations come from the server; until then the inbox is empty. */
val NoConversations: List<Conversation> = emptyList()

private val MsgTeal = Color(0xFF1CDBBA)

/** Total unread, used for the badge on the bottom tab. */
fun unreadTotal(list: List<Conversation> = NoConversations) = list.sumOf { it.unread }

@Composable
fun MessagesScreen(
    conversations: List<Conversation> = NoConversations,
    onOpen: (Conversation) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize().background(MsgTeal)) {
        Column(modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 16.dp)) {
            Text("Pesan", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(width = 20.dp, height = 4.dp).clip(CircleShape).background(Color.White))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 10.dp)) {
                if (conversations.isEmpty()) {
                    item { EmptyState("Belum ada pesan", "Percakapan kamu akan muncul di sini.") }
                }
                items(conversations, key = { it.id }) { c -> ConversationRow(c) { onOpen(c) } }
            }
        }
    }
}

@Composable
private fun ConversationRow(c: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationAvatar(c.kind, Modifier.size(64.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(c.title, color = Color(0xFF22222B), fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (c.preview.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(c.preview, color = Color(0xFFB0B0B8), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (c.time.isNotEmpty() || c.unread > 0) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.time, color = Color(0xFFB0B0B8), fontSize = 13.sp)
                if (c.unread > 0) UnreadDot(c.unread)
            }
        }
    }
}

@Composable
fun UnreadDot(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(22.dp).clip(CircleShape).background(Color(0xFFFF4B4B)),
        contentAlignment = Alignment.Center,
    ) { Text(if (count > 99) "99+" else "$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
}

/** Drawn avatars: a happy blob with a verified badge for the team, a gift box for notices. */
@Composable
private fun ConversationAvatar(kind: ConversationKind, modifier: Modifier) {
    when (kind) {
        ConversationKind.GIFT_NOTICE -> Canvas(
            modifier = modifier.clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1FE0A6), Color(0xFF12B8E6)))),
        ) {
            val w = size.width; val h = size.height
            drawRoundRect(Color(0xFFE8FAFF), Offset(w * 0.27f, h * 0.44f), Size(w * 0.46f, h * 0.3f), CornerRadius(w * 0.05f))
            drawRoundRect(Color(0xFFBFEFFA), Offset(w * 0.22f, h * 0.36f), Size(w * 0.56f, h * 0.12f), CornerRadius(w * 0.04f))
            drawRect(Color(0xFF3CC4E8), Offset(w * 0.47f, h * 0.36f), Size(w * 0.06f, h * 0.38f))
            drawCircle(Color(0xFFE8FAFF), radius = w * 0.07f, center = Offset(w * 0.42f, h * 0.31f))
            drawCircle(Color(0xFFE8FAFF), radius = w * 0.07f, center = Offset(w * 0.58f, h * 0.31f))
        }
        else -> Box(modifier = modifier) {
            Canvas(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF2ADBC0), Color(0xFF17C3B0))))) {
                val w = size.width; val h = size.height
                val heart = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.05f, h * 0.55f, w * 0.15f, h * 0.2f, w * 0.38f, h * 0.28f)
                    cubicTo(w * 0.46f, h * 0.31f, w * 0.5f, h * 0.38f, w * 0.5f, h * 0.38f)
                    cubicTo(w * 0.5f, h * 0.38f, w * 0.55f, h * 0.3f, w * 0.64f, h * 0.27f)
                    cubicTo(w * 0.9f, h * 0.2f, w * 0.97f, h * 0.55f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(heart, Brush.linearGradient(listOf(Color(0xFFFF7F6B), Color(0xFFFF4D6D))))
                drawCircle(Color.White, radius = w * 0.07f, center = Offset(w * 0.34f, h * 0.5f))
                drawCircle(Color(0xFF3A2A2A), radius = w * 0.035f, center = Offset(w * 0.34f, h * 0.5f))
                drawArc(Color(0xFF3A2A2A), 200f, 140f, false, Offset(w * 0.52f, h * 0.42f), Size(w * 0.2f, h * 0.12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.025f))
            }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).size(22.dp).clip(CircleShape).background(Color(0xFFFFC21A)),
                contentAlignment = Alignment.Center,
            ) { Text("V", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black) }
        }
    }
}
