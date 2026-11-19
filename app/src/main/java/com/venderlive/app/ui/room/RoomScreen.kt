package com.venderlive.app.ui.room

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.R
import com.venderlive.app.data.model.MicSeat
import com.venderlive.app.data.model.PkTeam
import com.venderlive.app.data.model.RoomTab
import com.venderlive.app.data.model.RoomUiState
import com.venderlive.app.ui.theme.BrandAccent
import com.venderlive.app.ui.theme.BrandPrimary
import com.venderlive.app.ui.theme.SurfaceCard
import com.venderlive.app.ui.theme.SurfaceDark

private val SeatSize = 62.dp
private val HostSize = 84.dp

@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    RoomContent(
        state = state,
        onSeatClick = viewModel::onSeatClick,
        onTabSelected = viewModel::selectTab,
        onDismissBanner = viewModel::dismissExpBanner,
        onToggleSpeaker = viewModel::toggleSpeaker,
        onToggleMic = viewModel::toggleMyMic,
        onExit = onExit,
    )
}

@Composable
fun RoomContent(
    state: RoomUiState,
    onSeatClick: (MicSeat) -> Unit,
    onTabSelected: (RoomTab) -> Unit,
    onDismissBanner: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMic: () -> Unit,
    onExit: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_room_default),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            RoomHeader(
                state = state,
                onExit = onExit,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )

            Spacer(Modifier.height(6.dp))

            HostRow(state = state, onSeatClick = onSeatClick)

            Spacer(Modifier.height(10.dp))

            SeatGrid(seats = state.seats, mySeatIndex = state.mySeatIndex, onSeatClick = onSeatClick)

            Spacer(Modifier.height(14.dp))

            RoomTabs(selected = state.selectedTab, onSelect = onTabSelected)

            if (state.expBannerVisible) {
                ExpBanner(onDismiss = onDismissBanner, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }

            AnnouncementBar(text = state.announcement, modifier = Modifier.padding(horizontal = 14.dp))

            Spacer(Modifier.weight(1f))

            RoomBottomBar(
                state = state,
                onToggleSpeaker = onToggleSpeaker,
                onToggleMic = onToggleMic,
            )
        }

        SideRail(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
        )
    }
}

@Composable
private fun RoomHeader(state: RoomUiState, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceCard.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "\uD83C\uDDEE\uD83C\uDDE9", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = state.countryLabel,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ID: ${state.roomNumber}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "\uD83D\uDC64 ${state.onlineCount}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }

            Spacer(Modifier.weight(1f))

            HeaderIcon(R.drawable.room_ic_grid_menu)
            HeaderIcon(R.drawable.room_ic_rocket)
            HeaderIcon(R.drawable.room_ic_power, onClick = onExit)
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.room_ic_trophy),
                contentDescription = null,
                tint = Color(0xFFFFC454),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(text = "0", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun HeaderIcon(icon: Int, onClick: () -> Unit = {}) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.9f),
        modifier = Modifier
            .padding(start = 10.dp)
            .size(22.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun HostRow(state: RoomUiState, onSeatClick: (MicSeat) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            SeatView(
                seat = state.hostSeat,
                size = HostSize,
                ringRes = R.drawable.room_ic_ring_owner,
                onClick = { onSeatClick(state.hostSeat) },
            )
        }
    }
}

@Composable
private fun SeatGrid(seats: List<MicSeat>, mySeatIndex: Int?, onSeatClick: (MicSeat) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        seats.chunked(4).forEach { rowSeats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                rowSeats.forEach { seat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SeatView(
                            seat = seat,
                            size = SeatSize,
                            ringRes = null,
                            isMine = mySeatIndex == seat.index,
                            onClick = { onSeatClick(seat) },
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${seat.index}",
                            color = if (seat.occupied) BrandAccent else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatView(
    seat: MicSeat,
    size: androidx.compose.ui.unit.Dp,
    ringRes: Int?,
    isMine: Boolean = false,
    onClick: () -> Unit,
) {
    val frame = when {
        seat.speaking -> R.drawable.room_ic_mic_speaking
        seat.pkTeam == PkTeam.RED -> R.drawable.room_ic_mic_pk_red
        seat.pkTeam == PkTeam.BLUE -> R.drawable.room_ic_mic_pk_blue
        seat.pkTeam == PkTeam.WINNER -> R.drawable.room_ic_mic_pk_mvp
        seat.locked -> R.drawable.room_ic_mic_lock
        seat.occupied -> R.drawable.room_ic_mic_occupied
        else -> R.drawable.room_ic_mic_empty
    }

    val effectiveRing = when {
        seat.cp -> R.drawable.room_ic_mic_cp
        else -> ringRes
    }

    Box(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(frame),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (effectiveRing != null) {
            Image(
                painter = painterResource(effectiveRing),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (seat.occupied) {
            AvatarBubble(label = seat.nickname.orEmpty(), size = size * 0.62f, speaking = seat.speaking)
        } else if (!seat.locked) {
            Icon(
                painter = painterResource(R.drawable.room_ic_bottom_mic_un_mute),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(size * 0.34f),
            )
        }

        if (seat.isHost) {
            Icon(
                painter = painterResource(R.drawable.room_ic_crown),
                contentDescription = null,
                tint = Color(0xFFFFC454),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(size * 0.28f),
            )
        }

        if (seat.occupied && seat.muted) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(SurfaceDark.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.room_ic_bottom_mic_mute),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(size * 0.2f),
                )
            }
        }

        if (isMine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.22f)
                    .clip(CircleShape)
                    .background(BrandAccent),
            )
        }
    }
}

@Composable
private fun AvatarBubble(label: String, size: androidx.compose.ui.unit.Dp, speaking: Boolean) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        if (speaking) BrandAccent else BrandPrimary,
                        SurfaceCard,
                    ),
                ),
            )
            .then(if (speaking) Modifier.border(2.dp, BrandAccent, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.take(1).uppercase(),
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun RoomTabs(selected: RoomTab, onSelect: (RoomTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TabLabel("Semua", selected == RoomTab.ALL) { onSelect(RoomTab.ALL) }
        TabLabel("Chat", selected == RoomTab.CHAT) { onSelect(RoomTab.CHAT) }
    }
}

@Composable
private fun TabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 22.dp else 0.dp)
                .height(2.dp)
                .background(if (selected) BrandAccent else Color.Transparent, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun ExpBanner(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFF9F43).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFFFF9F43).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Di mikrofon selama 10 menit +2.000 EXP",
            color = Color(0xFFFFC98A),
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "\u2715",
            color = Color(0xFFFFC98A),
            fontSize = 13.sp,
            modifier = Modifier.clickable(onClick = onDismiss),
        )
    }
}

@Composable
private fun AnnouncementBar(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceCard.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "\uD83D\uDCE2", fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun SideRail(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RailButton(R.drawable.room_ic_rocket)
        RailButton(R.drawable.room_ic_vault)
        RailButton(R.drawable.room_ic_game)
    }
}

@Composable
private fun RailButton(icon: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SurfaceCard.copy(alpha = 0.55f))
            .border(1.dp, BrandPrimary.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun RoomBottomBar(
    state: RoomUiState,
    onToggleSpeaker: () -> Unit,
    onToggleMic: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.92f))))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomAction(
            icon = R.drawable.room_ic_volume,
            tint = if (state.speakerOn) Color.White else Color.White.copy(alpha = 0.4f),
            onClick = onToggleSpeaker,
        )
        BottomAction(
            icon = if (state.muted) R.drawable.room_ic_bottom_mic_mute else R.drawable.room_ic_bottom_mic_un_mute,
            tint = if (state.muted) BrandAccent else Color.White,
            onClick = onToggleMic,
        )
        BottomAction(icon = R.drawable.room_ic_chat)
        BottomAction(icon = R.drawable.room_ic_emoji)
        Box(contentAlignment = Alignment.TopEnd) {
            BottomAction(icon = R.drawable.ic_nav_message)
            if (state.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(BrandAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "${state.unreadCount}", color = Color.White, fontSize = 9.sp)
                }
            }
        }
        BottomAction(icon = R.drawable.room_ic_grid_menu)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(BrandAccent, BrandPrimary))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.room_ic_gift),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun BottomAction(icon: Int, tint: Color = Color.White.copy(alpha = 0.9f), onClick: () -> Unit = {}) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
    )
}
