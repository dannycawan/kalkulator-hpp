package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.CalculationDao
import com.kalkulator.hpp.data.local.entity.CalculationResult
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    fun getAll(): Flow<List<CalculationResult>> = dao.getAll()
    fun searchByName(query: String): Flow<List<CalculationResult>> = dao.searchByName(query)
    fun getByDateRange(start: Long, end: Long): Flow<List<CalculationResult>> = dao.getByDateRange(start, end)
    fun getAverageHpp(): Flow<Double?> = dao.getAverageHpp()
    fun getAverageMargin(): Flow<Double?> = dao.getAverageMargin()
    fun getTopProfitable(): Flow<List<CalculationResult>> = dao.getTopProfitable()
    fun getTopExpensiveHpp(): Flow<List<CalculationResult>> = dao.getTopExpensiveHpp()
    fun getCount(): Flow<Int> = dao.getCount()
    suspend fun getById(id: Long): CalculationResult? = dao.getById(id)
    suspend fun insert(result: CalculationResult): Long = dao.insert(result)
    suspend fun update(result: CalculationResult) = dao.update(result)
    suspend fun delete(result: CalculationResult) = dao.delete(result)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)
}
