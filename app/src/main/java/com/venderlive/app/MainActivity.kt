package com.venderlive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.venderlive.app.debug.CrashLog
import com.venderlive.app.debug.CrashScreen
import com.venderlive.app.navigation.AppNav
import com.venderlive.app.ui.theme.VenderLiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VenderLiveTheme {
                val context = applicationContext
                var trace by remember { mutableStateOf(CrashLog.read(context)) }
                val current = trace
                if (current != null) {
                    CrashScreen(
                        text = current,
                        onClear = {
                            CrashLog.clear(context)
                            trace = null
                        },
                    )
                } else {
                    AppNav()
                }
            }
        }
    }
}
