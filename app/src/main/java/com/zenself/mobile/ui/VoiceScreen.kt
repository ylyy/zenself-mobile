package com.zenself.mobile.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenself.mobile.model.EventKind
import com.zenself.mobile.model.MobileEvent
import com.zenself.mobile.sync.EventWriter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var recognizedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        isListening = false
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            recognizedText = matches[0]
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语音记录") },
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
            // Status text
            Text(
                text = when {
                    recognizedText.isNotBlank() -> "你说了:"
                    isListening -> "正在听…"
                    else -> "点击麦克风，说出你现在的想法…"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transcribed text display
            if (recognizedText.isNotBlank()) {
                TextField(
                    value = recognizedText,
                    onValueChange = { recognizedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("识别结果") },
                    readOnly = false,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Mic button
            FloatingActionButton(
                onClick = {
                    if (isListening) return@FloatingActionButton
                    isListening = true
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "说出你现在的想法…")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    try {
                        speechLauncher.launch(intent)
                    } catch (_: ActivityNotFoundException) {
                        isListening = false
                        Toast.makeText(
                            context,
                            "此设备不支持语音识别",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                containerColor = if (isListening) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "开始语音输入",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Send button
            if (recognizedText.isNotBlank()) {
                Button(
                    onClick = {
                        scope.launch {
                            val event = MobileEvent(
                                kind = EventKind.VOICE,
                                text = recognizedText,
                            )
                            val ok = EventWriter.append(context, event)
                            val msg = if (ok) "已保存语音记录" else "保存失败，请重试"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (ok) recognizedText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("发送")
                }
            }
        }
    }
}
