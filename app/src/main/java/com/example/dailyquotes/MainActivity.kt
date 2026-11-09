package com.example.dailyquotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyquotes.ui.AppViewModel
import com.example.dailyquotes.ui.DailyQuotesApp
import com.example.dailyquotes.ui.theme.DailyQuotesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: AppViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            DailyQuotesTheme(darkTheme = settings.darkTheme) {
                DailyQuotesApp(viewModel)
            }
        }
    }
}
