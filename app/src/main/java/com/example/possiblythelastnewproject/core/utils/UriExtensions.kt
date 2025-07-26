package com.example.possiblythelastnewproject.core.utils

import android.net.Uri
import androidx.core.net.toUri

fun String?.toSafeUri(): Uri? {
    return this?.takeIf { it.isNotBlank() }
        ?.let { runCatching { it.toUri() }.getOrNull() }
}