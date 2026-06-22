package com.zenself.mobile.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.zenself.mobile.model.EventKind
import com.zenself.mobile.model.MobileEvent
import com.zenself.mobile.sync.EventWriter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var locationText by remember { mutableStateOf<String?>(null) }
    var nextEventText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    // Auto-grab location when permission is first acquired
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = client.lastLocation.await()
            location?.let {
                locationText = "${it.latitude}, ${it.longitude}"
            }
        } catch (_: Exception) {
            // Silently ignore; user can retry.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("位置打卡") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Location button / permission gate
            if (!hasPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("获取位置")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val client = LocationServices.getFusedLocationProviderClient(context)
                                val location: Location? = client.lastLocation.await()
                                locationText = if (location != null) {
                                    "${location.latitude}, ${location.longitude}"
                                } else {
                                    "无法获取位置"
                                }
                            } catch (_: Exception) {
                                locationText = "获取位置失败"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("刷新位置")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display location
            if (locationText != null) {
                Text(
                    text = "当前位置: $locationText",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else if (hasPermission) {
                Text(
                    text = "正在获取位置…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Optional next event text field
            TextField(
                value = nextEventText,
                onValueChange = { nextEventText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("下一个日程（可选）") },
                maxLines = 2,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    val loc = locationText ?: return@Button
                    scope.launch {
                        val event = MobileEvent(
                            kind = EventKind.CONTEXT,
                            location = loc,
                            nextEvent = nextEventText.ifBlank { null },
                        )
                        val ok = EventWriter.append(context, event)
                        val msg = if (ok) "打卡成功！" else "打卡失败，请重试"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            locationText = null
                            nextEventText = ""
                        }
                    }
                },
                enabled = locationText != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("打卡")
            }
        }
    }
}
