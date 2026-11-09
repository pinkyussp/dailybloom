package com.example.dailyquotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dailyquotes.ui.AppViewModel
import com.example.dailyquotes.ui.DailyQuotesApp
import com.example.dailyquotes.ui.LockScreen
import com.example.dailyquotes.ui.theme.DailyQuotesTheme

// FragmentActivity is needed for the biometric prompt used by the app lock.
class MainActivity : FragmentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val locked by viewModel.locked.collectAsStateWithLifecycle()

            DailyQuotesTheme(themeMode = settings.themeMode, accent = settings.accent) {
                if (locked) {
                    LockScreen(onUnlocked = viewModel::setUnlocked)
                } else {
                    DailyQuotesApp(viewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Lock again whenever the app leaves the foreground, if app lock is on.
        viewModel.lockIfEnabled()
    }
}
