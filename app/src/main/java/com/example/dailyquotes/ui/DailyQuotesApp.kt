package com.example.dailyquotes.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class AppTab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.Filled.WbSunny),
    Saved("Saved", Icons.Filled.Favorite),
    Settings("Settings", Icons.Filled.Settings)
}

/** Root composable: bottom navigation plus the selected tab's screen. */
@Composable
fun DailyQuotesApp(viewModel: AppViewModel) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(AppTab.Today) }

    val today by viewModel.today.collectAsStateWithLifecycle()
    val favourites by viewModel.favourites.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentTab) {
            AppTab.Today -> TodayScreen(
                state = today,
                isFavourite = { quote -> favourites.any { it.id == quote.id } },
                onNewQuote = viewModel::fetchNewQuote,
                onToggleFavourite = viewModel::toggleFavourite,
                onCopy = { copyQuote(context, it) },
                onShare = { shareQuote(context, it) },
                modifier = screenModifier
            )

            AppTab.Saved -> SavedScreen(
                favourites = favourites,
                onRemove = viewModel::removeFavourite,
                onCopy = { copyQuote(context, it) },
                onShare = { shareQuote(context, it) },
                modifier = screenModifier
            )

            AppTab.Settings -> SettingsScreen(
                settings = settings,
                onDarkThemeChange = viewModel::setDarkTheme,
                onReminderToggle = viewModel::setReminderEnabled,
                onReminderTimeChange = viewModel::setReminderTime,
                modifier = screenModifier
            )
        }
    }
}
