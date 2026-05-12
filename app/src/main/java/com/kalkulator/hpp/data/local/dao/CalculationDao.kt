package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.CalculationResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: CalculationResult): Long

    @Delete
    suspend fun delete(result: CalculationResult)

    @Query("DELETE FROM calculation_results")
    suspend fun deleteAll()

    @Query("SELECT * FROM calculation_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CalculationResult>>

    @Query("SELECT * FROM calculation_results WHERE id = :id")
    suspend fun getById(id: Long): CalculationResult?
}
