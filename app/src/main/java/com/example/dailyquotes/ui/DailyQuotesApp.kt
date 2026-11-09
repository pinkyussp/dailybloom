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
    Journal("Journal", Icons.Filled.Edit),
    Calendar("Calendar", Icons.Filled.DateRange),
    Settings("Settings", Icons.Filled.Settings)
}

/** Root composable: a swipeable pager of the three screens, with a bottom bar kept in sync. */
@Composable
fun DailyQuotesApp(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { AppTab.entries.size })
    val todayDate = remember { LocalDate.now() }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
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
                AppTab.Journal -> JournalScreen(
                    entry = entries[todayDate],
                    showPrompt = settings.showPrompts,
                    streak = currentStreak(entries, todayDate),
                    onUpdate = viewModel::updateToday,
                    onAddPhotos = viewModel::addPhotos,
                    onRemovePhoto = viewModel::removePhoto
                )

                AppTab.Calendar -> CalendarScreen(
                    entries = entries,
                    firstDay = firstDay,
                    today = todayDate,
                    weekStartsMonday = settings.weekStartsMonday
                )

                AppTab.Settings -> SettingsScreen(
                    settings = settings,
                    viewModel = viewModel
                )
            }
        }
    }
}
