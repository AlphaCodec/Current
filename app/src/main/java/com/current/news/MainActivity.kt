package com.current.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.current.news.navigation.CurrentNavGraph
import com.current.news.ui.theme.CurrentTheme
import com.current.news.ui.theme.Ink

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        setContent {
            CurrentTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    CurrentNavGraph()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Bars can be pulled back by the system (e.g. after a dialog, keyboard,
        // or app switch) — re-hide them once the window regains focus so the
        // app stays fullscreen rather than leaving a stale system bar visible.
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        // Swipe from an edge to reveal the bars temporarily, then they
        // auto-hide again — standard "immersive sticky" behavior rather
        // than locking the user out of the bars entirely.
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
