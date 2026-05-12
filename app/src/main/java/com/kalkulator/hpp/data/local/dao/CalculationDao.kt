package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.CalculationResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: CalculationResult): Long

    @Update
    suspend fun update(result: CalculationResult)

    @Delete
    suspend fun delete(result: CalculationResult)

    @Query("DELETE FROM calculation_results")
    suspend fun deleteAll()

    @Query("DELETE FROM calculation_results WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM calculation_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CalculationResult>>

    @Query("SELECT * FROM calculation_results WHERE id = :id")
    suspend fun getById(id: Long): CalculationResult?

    @Query("SELECT * FROM calculation_results WHERE recipeName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchByName(query: String): Flow<List<CalculationResult>>

    @Query("SELECT * FROM calculation_results WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<CalculationResult>>

    @Query("SELECT AVG(hppPerUnit) FROM calculation_results")
    fun getAverageHpp(): Flow<Double?>

    @Query("SELECT AVG(marginPct) FROM calculation_results")
    fun getAverageMargin(): Flow<Double?>

    @Query("SELECT * FROM calculation_results ORDER BY suggestedPrice - hppPerUnit DESC LIMIT 5")
    fun getTopProfitable(): Flow<List<CalculationResult>>

    @Query("SELECT * FROM calculation_results ORDER BY hppPerUnit DESC LIMIT 5")
    fun getTopExpensiveHpp(): Flow<List<CalculationResult>>

    @Query("SELECT COUNT(*) FROM calculation_results")
    fun getCount(): Flow<Int>
}
