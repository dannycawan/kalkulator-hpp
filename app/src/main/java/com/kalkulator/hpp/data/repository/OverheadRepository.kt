package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.OverheadDao
import com.kalkulator.hpp.data.local.entity.OverheadItem
import kotlinx.coroutines.flow.Flow

class OverheadRepository(private val dao: OverheadDao) {
    fun getAll(): Flow<List<OverheadItem>> = dao.getAll()
    fun getTotalMonthlyCost(): Flow<Double> = dao.getTotalMonthlyCost()
    fun getCount(): Flow<Int> = dao.getCount()
    suspend fun getById(id: Long): OverheadItem? = dao.getById(id)
    suspend fun insert(item: OverheadItem): Long = dao.insert(item)
    suspend fun update(item: OverheadItem) = dao.update(item)
    suspend fun delete(item: OverheadItem) = dao.delete(item)
}
