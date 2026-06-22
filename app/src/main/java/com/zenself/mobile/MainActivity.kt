package com.zenself.mobile

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.zenself.mobile.ui.ClipboardScreen
import com.zenself.mobile.ui.LocationScreen
import com.zenself.mobile.ui.MoodScreen
import com.zenself.mobile.ui.SettingsScreen
import com.zenself.mobile.ui.VoiceScreen
import com.zenself.mobile.ui.theme.ZenSelfTheme

private data class BottomTab(val label: String, val emoji: String)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenSelfTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val tabs = listOf(
        BottomTab("语音倾诉", "\uD83C\uDFA4️"),
        BottomTab("心情打卡", "\uD83D\uDE0A"),
        BottomTab("复制即记", "\uD83D\uDCCB"),
        BottomTab("地点打卡", "\uD83D\uDCCD"),
    )

    // Build permission list: always RECORD_AUDIO + ACCESS_FINE_LOCATION,
    // plus POST_NOTIFICATIONS on Android 13+.
    val permissionsToRequest = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toList()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* silently handle; re-requestable from Settings screen */ }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\uD83E\uDDD8 ZenSelf") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Text(tab.emoji) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> VoiceScreen()
                1 -> MoodScreen()
                2 -> ClipboardScreen()
                3 -> LocationScreen()
            }
        }
    }
}
