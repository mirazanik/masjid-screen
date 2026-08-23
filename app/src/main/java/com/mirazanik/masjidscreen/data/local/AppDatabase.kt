package com.mirazanik.masjidscreen.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mirazanik.masjidscreen.data.local.dao.*
import com.mirazanik.masjidscreen.data.local.entity.*

@Database(
    entities = [
        MosqueConfigEntity::class,
        JamaatTimesEntity::class,
        HadithEntity::class,
        NoticeEntity::class,
        ScreenInfoEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mosqueConfigDao(): MosqueConfigDao
    abstract fun jamaatTimesDao(): JamaatTimesDao
    abstract fun hadithDao(): HadithDao
    abstract fun noticeDao(): NoticeDao
    abstract fun screenInfoDao(): ScreenInfoDao

    suspend fun clearCache() {
        mosqueConfigDao().deleteAll()
        jamaatTimesDao().deleteAll()
        hadithDao().deleteAll()
        noticeDao().deleteAll()
        screenInfoDao().deleteAll()
    }

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mosque_db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}
