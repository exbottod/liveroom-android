package com.venderlive.app.ui.room

import androidx.lifecycle.ViewModel
import com.venderlive.app.data.model.MicSeat
import com.venderlive.app.data.model.PkTeam
import com.venderlive.app.data.model.RoomTab
import com.venderlive.app.data.model.RoomUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Room state holder. Phase 2 wires this to the VPS room API and Agora; for now it
 * drives the UI with local state so the layout can be built and reviewed.
 */
class RoomViewModel(roomId: String = "1") : ViewModel() {

    private val _state = MutableStateFlow(
        RoomUiState(
            roomId = roomId,
            title = "Room Santai",
            roomNumber = "613583246",
            onlineCount = 1,
            announcement = "Selamat datang di room VenderLive",
            hostSeat = MicSeat(index = 0, userId = "host", nickname = "BANG EX", level = 12, isHost = true),
            seats = (1..12).map { i ->
                when (i) {
                    1 -> MicSeat(index = 1, userId = "u1", nickname = "Vivi", level = 3, pkTeam = PkTeam.RED)
                    2 -> MicSeat(index = 2, userId = "u2", nickname = "Rian", level = 1, muted = true, pkTeam = PkTeam.BLUE)
                    5 -> MicSeat(index = 5, locked = true)
                    7 -> MicSeat(index = 7, userId = "u3", nickname = "Sari", level = 5, speaking = true)
                    3 -> MicSeat(index = 3, userId = "u4", nickname = "Dewi", level = 4, cp = true)
                    else -> MicSeat(index = i)
                }
            },
        ),
    )
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    fun selectTab(tab: RoomTab) = _state.update { it.copy(selectedTab = tab) }

    fun dismissExpBanner() = _state.update { it.copy(expBannerVisible = false) }

    fun toggleSpeaker() = _state.update { it.copy(speakerOn = !it.speakerOn) }

    fun toggleMyMic() = _state.update { it.copy(muted = !it.muted) }

    fun onSeatClick(seat: MicSeat) {
        if (seat.locked) return
        _state.update { current ->
            val mine = current.mySeatIndex
            when {
                seat.occupied -> current
                mine == seat.index -> current.copy(mySeatIndex = null)
                else -> current.copy(mySeatIndex = seat.index)
            }
        }
    }
}
