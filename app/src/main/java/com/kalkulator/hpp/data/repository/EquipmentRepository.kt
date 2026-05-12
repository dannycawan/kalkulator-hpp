package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.EquipmentDao
import com.kalkulator.hpp.data.local.entity.Equipment
import kotlinx.coroutines.flow.Flow

class EquipmentRepository(private val dao: EquipmentDao) {
    fun getAll(): Flow<List<Equipment>> = dao.getAll()
    fun getCount(): Flow<Int> = dao.getCount()
    suspend fun getById(id: Long): Equipment? = dao.getById(id)
    suspend fun insert(equipment: Equipment): Long = dao.insert(equipment)
    suspend fun update(equipment: Equipment) = dao.update(equipment)
    suspend fun delete(equipment: Equipment) = dao.delete(equipment)
}
