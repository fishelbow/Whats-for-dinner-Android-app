package com.example.possiblythelastnewproject.core.utils

fun truncateWithEllipsis(text: String, maxChars: Int = 15): String {
    return if (text.length > maxChars) text.take(maxChars).trimEnd() + "…" else text
}