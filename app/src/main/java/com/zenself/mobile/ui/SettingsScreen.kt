package com.zenself.mobile.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenself.mobile.sync.EventWriter

private fun Context.putBooleanPref(key: String, value: Boolean) =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .edit().putBoolean(key, value).apply()

private fun Context.getBooleanPref(key: String, default: Boolean = false): Boolean =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .getBoolean(key, default)

private fun Context.putIntPref(key: String, value: Int) =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .edit().putInt(key, value).apply()

private fun Context.getIntPref(key: String, default: Int = 0): Int =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .getInt(key, default)

private fun Context.putStringPref(key: String, value: String?) =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .edit().putString(key, value).apply()

private fun Context.getStringPref(key: String, default: String? = null): String? =
    getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
        .getString(key, default)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var syncDir by rememberSaveable {
        mutableStateOf(
            context.getStringPref("sync_dir") ?: EventWriter.defaultDir(context).absolutePath
        )
    }

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            syncDir = it.toString()
            context.putStringPref("sync_dir", it.toString())
        }
    }

    val intervalOptions = listOf(15, 30, 60)
    var selectedInterval by rememberSaveable {
        mutableStateOf(context.getIntPref("metrics_interval_minutes", 30))
    }
    var intervalExpanded by rememberSaveable { mutableStateOf(false) }

    var locationEnabled by rememberSaveable {
        mutableStateOf(context.getBooleanPref("location_enabled", false))
    }

    fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("同步目录", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = syncDir,
                onValueChange = {},
                label = { Text("当前同步目录") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { directoryPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择目录")
            }

                        androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Text("采集间隔", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = intervalExpanded,
                onExpandedChange = { intervalExpanded = !intervalExpanded }
            ) {
                OutlinedTextField(
                    value = "$selectedInterval 分钟",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("间隔时长") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = intervalExpanded,
                    onDismissRequest = { intervalExpanded = false }
                ) {
                    intervalOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("$option 分钟") },
                            onClick = {
                                selectedInterval = option
                                intervalExpanded = false
                                context.putIntPref("metrics_interval_minutes", option)
                            }
                        )
                    }
                }
            }

                        androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Text("地点触发打卡", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("到达/离开指定地点时自动打卡", modifier = Modifier.weight(1f))
                Switch(
                    checked = locationEnabled,
                    onCheckedChange = {
                        locationEnabled = it
                        context.putBooleanPref("location_enabled", it)
                    }
                )
            }

                        androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Text("权限管理", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            PermissionRow(
                name = "位置权限",
                granted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
                onRequest = { openAppSettings() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PermissionRow(
                name = "麦克风权限",
                granted = hasPermission(Manifest.permission.RECORD_AUDIO),
                onRequest = { openAppSettings() }
            )
            Spacer(modifier = Modifier.height(8.dp))

            val usageStatsGranted = remember {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
                val mode = appOps?.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
                mode == android.app.AppOpsManager.MODE_ALLOWED
            }
            PermissionRow(
                name = "使用时长统计",
                granted = usageStatsGranted,
                onRequest = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(name, modifier = Modifier.weight(1f))
        Text(
            text = if (granted) "已授权" else "未授权",
            color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        if (!granted) {
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Button(onClick = onRequest) { Text("设置") }
        }
    }
}
