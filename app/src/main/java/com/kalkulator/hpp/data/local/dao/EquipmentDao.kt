package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.Equipment
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: Equipment): Long

    @Update
    suspend fun update(equipment: Equipment)

    @Delete
    suspend fun delete(equipment: Equipment)

    @Query("SELECT * FROM equipment ORDER BY name ASC")
    fun getAll(): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getById(id: Long): Equipment?

    @Query("SELECT COUNT(*) FROM equipment")
    fun getCount(): Flow<Int>
}
