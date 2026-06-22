package com.zenself.mobile.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import com.zenself.mobile.model.Mood
import com.zenself.mobile.model.MobileEvent
import com.zenself.mobile.sync.EventWriter
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var noteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情打卡") },
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
                text = "你现在感觉怎么样？",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emoji buttons — one per Mood entry
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Mood.entries.forEach { mood ->
                    FilledTonalButton(
                        onClick = { selectedMood = mood },
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedMood == mood) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Text(
                            text = mood.emoji,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood label
            selectedMood?.let { mood ->
                Text(
                    text = mood.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Optional note field
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("写点什么（可选）") },
                maxLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    val mood = selectedMood ?: return@Button
                    scope.launch {
                        val event = MobileEvent(
                            kind = EventKind.CHECKIN,
                            mood = mood,
                            text = noteText.ifBlank { null },
                        )
                        val ok = EventWriter.append(context, event)
                        val msg = if (ok) "打卡成功！" else "打卡失败，请重试"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            selectedMood = null
                            noteText = ""
                        }
                    }
                },
                enabled = selectedMood != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("打卡")
            }
        }
    }
}
