package com.example.possiblythelastnewproject.core.data.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BackupSerializer @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }
    private val supportedVersion = 1

    fun serialize(backup: FullDatabaseBackup): String = json.encodeToString(backup)

    fun deserialize(jsonString: String): Result<FullDatabaseBackup> = runCatching {
        val backup = json.decodeFromString<FullDatabaseBackup>(jsonString)
        if (backup.version > supportedVersion) {
            throw IllegalArgumentException("Unsupported backup version: ${backup.version}")
        }
        backup
    }
}