package com.crystal.samplecalculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.crystal.samplecalculator.model.History

@Dao
interface HistoryDao {
    //조회, 추가, 삭제 어떻게 할지 정의한다. room db에 연결된다.

    //쿼리문을 작성해준다
    @Query("select * from history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("delete from history")
    fun deleteAll()

//    @Delete
//    fun delete(history: History)

//    @Query("select * from history where result like :result limit 1")
//    fun findByResult(result: String)
}