package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.OverheadItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OverheadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OverheadItem): Long

    @Update
    suspend fun update(item: OverheadItem)

    @Delete
    suspend fun delete(item: OverheadItem)

    @Query("SELECT * FROM overhead_items ORDER BY name ASC")
    fun getAll(): Flow<List<OverheadItem>>

    @Query("SELECT * FROM overhead_items WHERE id = :id")
    suspend fun getById(id: Long): OverheadItem?

    @Query("SELECT COALESCE(SUM(monthlyCost), 0.0) FROM overhead_items")
    fun getTotalMonthlyCost(): Flow<Double>

    @Query("SELECT COUNT(*) FROM overhead_items")
    fun getCount(): Flow<Int>
}
