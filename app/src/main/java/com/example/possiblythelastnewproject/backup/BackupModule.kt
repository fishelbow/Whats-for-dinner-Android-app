package com.example.possiblythelastnewproject.backup

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import com.example.possiblythelastnewproject.core.data.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideZipImporter(
        @ApplicationContext context: Context,
        db: AppDatabase
    ): ZipImporter = ZipImporter(context, db)

    @Provides
    @Singleton
    fun provideZipExporter(
        @ApplicationContext context: Context,
        db: AppDatabase
    ): ZipExporter = ZipExporter(context, db)
    @Provides
    @Singleton
    fun provideZipBackupRepository(
        importer: ZipImporter,
        exporter: ZipExporter
    ): ZipBackupRepository = ZipBackupRepository(importer, exporter)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }


}