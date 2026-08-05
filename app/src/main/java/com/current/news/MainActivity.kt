package com.current.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.current.news.navigation.CurrentNavGraph
import com.current.news.ui.theme.CurrentTheme
import com.current.news.ui.theme.Ink

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrentTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    CurrentNavGraph()
                }
            }
        }
    }
}
