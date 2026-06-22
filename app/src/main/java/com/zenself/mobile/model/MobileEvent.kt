package com.zenself.mobile.model

import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Unified event schema — one JSON line per entry in mobile_buffer.jsonl.
 * Matches the contract in ZenSelf Mac-side docs/mobile-setup/android-setup.md §3.
 */
enum class EventKind(val value: String) {
    VOICE("voice"),
    CHECKIN("checkin"),
    METRIC("metric"),
    CONTEXT("context"),
    CLIPBOARD("clipboard");

    companion object {
        fun fromValue(v: String): EventKind = entries.firstOrNull { it.value == v } ?: VOICE
    }
}

enum class Mood(val value: String, val label: String, val emoji: String) {
    ANXIOUS("anxious", "焦虑", "😰"),
    FOCUSED("focused", "专注", "🎯"),
    SAD("sad", "低落", "🌧️"),
    RELAXED("relaxed", "放松", "☕"),
    DISTRACTED("distracted", "分心", "🍃");

    companion object {
        fun fromValue(v: String): Mood = entries.firstOrNull { it.value == v } ?: FOCUSED
    }
}

data class MobileEvent(
    val ts: String = Instant.now().toString(),
    val kind: EventKind,
    val text: String? = null,
    val mood: Mood? = null,
    val heartRate: Int? = null,
    val screenTimeMin: Int? = null,
    val location: String? = null,
    val nextEvent: String? = null,
) {
    /**
     * Serialize to a single JSON line. Null fields are omitted entirely so
     * the Mac-side reader only sees keys that carry signal.
     */
    fun toJsonLine(): String {
        val obj = JSONObject()
        obj.put("ts", ts)
        obj.put("kind", kind.value)
        text?.let { obj.put("text", it) }
        mood?.let { obj.put("mood", it.value) }
        heartRate?.let { obj.put("heart_rate", it) }
        screenTimeMin?.let { obj.put("screen_time_min", it) }
        location?.let { obj.put("location", it) }
        nextEvent?.let { obj.put("next_event", it) }
        return obj.toString()
    }

    companion object {
        fun fromJsonLine(line: String): MobileEvent? {
            return try {
                val obj = JSONObject(line)
                MobileEvent(
                    ts = obj.optString("ts", Instant.now().toString()),
                    kind = EventKind.fromValue(obj.getString("kind")),
                    text = obj.optString("text", null).ifBlank { null },
                    mood = obj.optString("mood", null).ifBlank { null }?.let { Mood.fromValue(it) },
                    heartRate = obj.optInt("heart_rate", -1).takeIf { it >= 0 },
                    screenTimeMin = obj.optInt("screen_time_min", -1).takeIf { it >= 0 },
                    location = obj.optString("location", null).ifBlank { null },
                    nextEvent = obj.optString("next_event", null).ifBlank { null },
                )
            } catch (_: Exception) { null }
        }
    }
}
