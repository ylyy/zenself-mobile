package com.zenself.mobile.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenself.mobile.model.EventKind
import com.zenself.mobile.model.MobileEvent
import com.zenself.mobile.sync.EventWriter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var clipboardText by remember { mutableStateOf("") }

    // Read clipboard on first composition
    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            clipboardText = clip.getItemAt(0).coerceToText(context).toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("剪贴板") },
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
            Text(
                text = "当前剪贴板内容",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Clipboard preview
            if (clipboardText.isNotBlank()) {
                Text(
                    text = clipboardText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            } else {
                Text(
                    text = "剪贴板为空",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val text = clipboardText
                    if (text.isBlank()) {
                        Toast.makeText(context, "没有可记录的内容", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        val event = MobileEvent(
                            kind = EventKind.CLIPBOARD,
                            text = text,
                        )
                        val ok = EventWriter.append(context, event)
                        val msg = if (ok) "已记录" else "记录失败，请重试"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) clipboardText = ""
                    }
                },
                enabled = clipboardText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("记录这条内容")
            }
        }
    }
}
