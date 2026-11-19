package com.venderlive.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colours of the light "Beranda" feed. Kept here so the whole screen is easy to retune. */
object FeedColors {
    val HeaderTop = Color(0xFF2FD3B4)
    val HeaderBottom = Color(0xFFD9F7EE)
    val Page = Color(0xFFFFFFFF)
    val ChipOn = Color(0xFFFFD54F)
    val ChipOff = Color(0xFFF3F3F5)
    val Text = Color(0xFF2A2A33)
    val TextSoft = Color(0xFF8A8A96)
    val Rank = Brush.linearGradient(listOf(Color(0xFF34C9A6), Color(0xFF1FA98A)))
    val Cp = Brush.linearGradient(listOf(Color(0xFFB69CFF), Color(0xFF9B7BF2)))
    val Family = Brush.linearGradient(listOf(Color(0xFFFFB866), Color(0xFFFF9F43)))
}

data class FeedRoom(
    val id: String,
    val title: String,
    val listeners: Int,
    val flag: String,
    val tint: Color,
    val pk: Boolean = false,
)

val SampleFeedRooms = listOf(
    FeedRoom("r1", "welcome", 1, "ID", Color(0xFFC9C3BB)),
    FeedRoom("r2", "SPALEE NORIM", 8, "RS", Color(0xFFD7B8F2), pk = true),
    FeedRoom("r3", "Gulya", 30, "KZ", Color(0xFF9FB0B8), pk = true),
    FeedRoom("r4", "adyb", 8, "EG", Color(0xFFB59C86), pk = true),
    FeedRoom("r5", "Malam Santai", 14, "ID", Color(0xFF8FA8D8)),
    FeedRoom("r6", "Ngopi Bareng", 5, "ID", Color(0xFFE0B7A0)),
)

private val FeedTabs = listOf("Milik saya", "Populer", "Berpesta")
private val FeedChips = listOf("Panas", "Baru", "Permainan", "PK")

@Composable
fun HomeFeed(
    onOpenRoom: (String) -> Unit,
    onSearch: () -> Unit = {},
    onCreateRoom: () -> Unit = {},
    rooms: List<FeedRoom> = SampleFeedRooms,
) {
    var tab by remember { mutableIntStateOf(1) }
    var chip by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(FeedColors.Page)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(listOf(FeedColors.HeaderTop, FeedColors.HeaderBottom, FeedColors.Page))),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                FeedHeader(tab, { tab = it }, onSearch, onCreateRoom)
            }
            item(span = { GridItemSpan(2) }) { BannerCarousel() }
            item(span = { GridItemSpan(2) }) { ShortcutRow() }
            item(span = { GridItemSpan(2) }) { ChipRow(chip) { chip = it } }
            items(rooms) { room -> RoomCard(room) { onOpenRoom(room.title) } }
        }
    }
}

@Composable
private fun FeedHeader(selected: Int, onSelect: (Int) -> Unit, onSearch: () -> Unit, onCreate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.35f))
                .clickable(onClick = onCreate),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFB347)),
                contentAlignment = Alignment.Center,
            ) { Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeedTabs.forEachIndexed { i, label ->
                val on = i == selected
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(i) }) {
                    Text(
                        label,
                        color = if (on) Color.White else Color.White.copy(alpha = 0.78f),
                        fontSize = if (on) 20.sp else 17.sp,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        Modifier.size(width = 22.dp, height = 3.dp).clip(CircleShape)
                            .background(if (on) Color.White else Color.Transparent),
                    )
                }
            }
        }
        SearchGlyph(onSearch)
    }
}

@Composable
private fun SearchGlyph(onClick: () -> Unit) {
    Canvas(modifier = Modifier.size(28.dp).clickable(onClick = onClick)) {
        val w = size.minDimension * 0.09f
        drawCircle(Color.White, radius = size.minDimension * 0.30f, center = Offset(size.width * 0.44f, size.height * 0.44f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w))
        drawLine(Color.White, Offset(size.width * 0.66f, size.height * 0.66f), Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
private fun BannerCarousel() {
    val slides = listOf(
        Triple("Pertarungan", "Mikrofon", Brush.linearGradient(listOf(Color(0xFF5B2AA8), Color(0xFFC1264B), Color(0xFF7A2AD0)))),
        Triple("Hadiah", "Harian", Brush.linearGradient(listOf(Color(0xFF1F6FEB), Color(0xFF7C4DFF)))),
        Triple("Event", "Akhir Pekan", Brush.linearGradient(listOf(Color(0xFFFF7A59), Color(0xFFFF4D8D)))),
    )
    val pager = rememberPagerState(pageCount = { slides.size })
    Column {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().height(116.dp)) { page ->
            val (a, b, brush) = slides[page]
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(brush),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(a, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(b, color = Color(0xFFFFE08A), fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(slides.size) { i ->
                Box(
                    Modifier.padding(horizontal = 3.dp).size(if (i == pager.currentPage) 14.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (i == pager.currentPage) Color(0xFF2FD3B4) else Color(0xFFD0D0D6)),
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Shortcut("Peringkat", FeedColors.Rank, Modifier.weight(1f))
        Shortcut("CP", FeedColors.Cp, Modifier.weight(1f))
        Shortcut("Keluarga", FeedColors.Family, Modifier.weight(1f))
    }
}

@Composable
private fun Shortcut(label: String, brush: Brush, modifier: Modifier) {
    Box(
        modifier = modifier.height(84.dp).clip(RoundedCornerShape(12.dp)).background(brush).padding(10.dp),
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopStart))
        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
            repeat(3) { i ->
                Box(
                    Modifier.padding(horizontal = 2.dp).size(if (i == 1) 34.dp else 28.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                        .border(BorderStroke(1.5.dp, Color.White), CircleShape),
                )
            }
        }
    }
}

@Composable
private fun ChipRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FeedChips.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50))
                    .background(if (on) FeedColors.ChipOn else FeedColors.ChipOff)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) {
                Text(
                    label,
                    color = if (on) Color(0xFF3A2E00) else FeedColors.Text,
                    fontSize = 15.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun RoomCard(room: FeedRoom, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(room.tint, room.tint.copy(alpha = 0.72f))))
                .then(if (room.pk) Modifier.border(BorderStroke(2.dp, Color(0xFFFF9A3C)), RoundedCornerShape(14.dp)) else Modifier),
        ) {
            if (room.pk) {
                Text("PK", color = Color(0xFFFFD54F), fontSize = 13.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            }
            Row(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Equalizer()
                Spacer(Modifier.width(4.dp))
                Text("${room.listeners}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(modifier = Modifier.padding(top = 5.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            FlagChip(room.flag)
            Spacer(Modifier.width(6.dp))
            Text(room.title, color = FeedColors.Text, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Equalizer() {
    Canvas(modifier = Modifier.size(width = 12.dp, height = 10.dp)) {
        val bw = size.width / 5f
        listOf(0.5f, 1f, 0.7f).forEachIndexed { i, h ->
            drawRect(Color.White, topLeft = Offset(i * bw * 2f, size.height * (1f - h)),
                size = androidx.compose.ui.geometry.Size(bw, size.height * h))
        }
    }
}

/** Small rounded country tag. Real flag art is a later asset slot (flag_xx). */
@Composable
private fun FlagChip(code: String) {
    Box(
        modifier = Modifier.size(width = 24.dp, height = 16.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE9E9EE)),
        contentAlignment = Alignment.Center,
    ) { Text(code, color = FeedColors.TextSoft, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
}
