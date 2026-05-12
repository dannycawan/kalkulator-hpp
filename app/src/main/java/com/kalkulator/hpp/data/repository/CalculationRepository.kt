package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.CalculationDao
import com.kalkulator.hpp.data.local.entity.CalculationResult
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    fun getAll(): Flow<List<CalculationResult>> = dao.getAll()
    suspend fun insert(result: CalculationResult): Long = dao.insert(result)
    suspend fun delete(result: CalculationResult) = dao.delete(result)
    suspend fun deleteAll() = dao.deleteAll()
}
