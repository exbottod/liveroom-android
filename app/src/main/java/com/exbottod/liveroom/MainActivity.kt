package com.exbottod.liveroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.exbottod.liveroom.navigation.AppNav
import com.exbottod.liveroom.ui.theme.LiveRoomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveRoomTheme {
                AppNav()
            }
        }
    }
}
