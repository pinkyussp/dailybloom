package com.example.dailyquotes.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppTab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.Filled.Edit),
    Calendar("Calendar", Icons.Filled.DateRange),
    Settings("Settings", Icons.Filled.Settings)
}

/**
 * Root composable: a swipeable pager of the three screens, with a bottom navigation bar
 * that stays in sync with it.
 */
@Composable
fun DailyQuotesApp(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { AppTab.entries.size })
    val todayDate = remember { LocalDate.now() }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val firstDay by viewModel.firstDay.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            when (AppTab.entries[page]) {
                AppTab.Today -> TodayScreen(
                    date = todayDate,
                    savedText = notes[todayDate],
                    onSave = { viewModel.saveNote(todayDate, it) }
                )

                AppTab.Calendar -> CalendarScreen(
                    notes = notes,
                    firstDay = firstDay,
                    today = todayDate
                )

                AppTab.Settings -> SettingsScreen(
                    settings = settings,
                    onDarkThemeChange = viewModel::setDarkTheme,
                    onReminderToggle = viewModel::setReminderEnabled,
                    onReminderTimeChange = viewModel::setReminderTime
                )
            }
        }
    }
}
