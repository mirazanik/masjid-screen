package com.mirazanik.masjidscreen.data.local.dao

import androidx.room.*
import com.mirazanik.masjidscreen.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueConfigDao {
    @Query("SELECT * FROM mosque_config WHERE id = 'default'")
    fun observe(): Flow<MosqueConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MosqueConfigEntity)

    @Query("DELETE FROM mosque_config")
    suspend fun deleteAll()
}

@Dao
interface JamaatTimesDao {
    @Query("SELECT * FROM jamaat_times WHERE id = 'default'")
    fun observe(): Flow<JamaatTimesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: JamaatTimesEntity)

    @Query("DELETE FROM jamaat_times")
    suspend fun deleteAll()
}

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadith WHERE active = 1")
    fun observeActive(): Flow<List<HadithEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HadithEntity>)

    @Query("DELETE FROM hadith")
    suspend fun deleteAll()
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices WHERE active = 1 ORDER BY priority ASC")
    fun observeActive(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<NoticeEntity>)

    @Query("DELETE FROM notices")
    suspend fun deleteAll()
}

@Dao
interface ScreenInfoDao {
    @Query("SELECT * FROM screen_info WHERE screenId = :screenId")
    fun observe(screenId: String): Flow<ScreenInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScreenInfoEntity)

    @Query("DELETE FROM screen_info")
    suspend fun deleteAll()
}
