package com.zenself.mobile.sync

import android.content.Context
import android.os.Environment
import com.zenself.mobile.model.MobileEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

/**
 * Appends a [MobileEvent] as a single JSON line to the sync buffer file.
 *
 * The default path is `Downloads/ZenSelfMobileSync/mobile_buffer.jsonl` —
 * a publicly accessible directory that Syncthing can directly sync.
 * Users can override via Settings (stored in DataStore).
 */
object EventWriter {

    private const val DEFAULT_RELATIVE_PATH = "ZenSelfMobileSync/mobile_buffer.jsonl"
    const val FILENAME = "mobile_buffer.jsonl"

    fun defaultDir(context: Context): File {
        // Android 10+ uses the shared Downloads via MediaStore
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ZenSelfMobileSync"
        )
    }

    fun defaultFile(context: Context): File {
        return File(defaultDir(context), FILENAME)
    }

    /**
     * Resolve the actual buffer file, respecting user overrides from prefs.
     * Falls back to default if the override path is empty or invalid.
     */
    suspend fun resolveFile(context: Context, customPath: String? = null): File {
        val path = customPath
        if (!path.isNullOrBlank()) {
            val f = File(path)
            if (f.parentFile?.exists() == true || f.parentFile?.mkdirs() == true) {
                return f
            }
        }
        val dir = defaultDir(context)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, FILENAME)
    }

    /**
     * Append one event. Thread-safe via synchronized FileWriter.
     * Returns true on success.
     */
    suspend fun append(context: Context, event: MobileEvent, customPath: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = resolveFile(context, customPath)
                // Ensure parent exists
                file.parentFile?.mkdirs()
                FileWriter(file, true).use { writer ->
                    writer.append(event.toJsonLine())
                    writer.append("\n")
                    writer.flush()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
