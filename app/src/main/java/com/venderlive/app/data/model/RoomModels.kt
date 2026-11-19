package com.venderlive.app.data.model

/** One mic seat in an audio room. [index] is 1-based and doubles as the RTC uid. */
data class MicSeat(
    val index: Int,
    val userId: String? = null,
    val nickname: String? = null,
    val level: Int = 0,
    val muted: Boolean = false,
    val locked: Boolean = false,
    val speaking: Boolean = false,
    val isHost: Boolean = false,
) {
    val occupied: Boolean get() = userId != null
}

enum class RoomTab { ALL, CHAT }

data class RoomUiState(
    val roomId: String = "",
    val title: String = "",
    val countryLabel: String = "INDONESIA",
    val roomNumber: String = "0",
    val onlineCount: Int = 0,
    val hostSeat: MicSeat = MicSeat(index = 0, isHost = true),
    val seats: List<MicSeat> = emptyList(),
    val selectedTab: RoomTab = RoomTab.ALL,
    val announcement: String = "",
    val expBannerVisible: Boolean = true,
    val mySeatIndex: Int? = null,
    val muted: Boolean = false,
    val speakerOn: Boolean = true,
    val unreadCount: Int = 0,
)
