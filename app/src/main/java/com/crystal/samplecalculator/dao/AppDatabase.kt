package com.crystal.samplecalculator.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crystal.samplecalculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun historyDao(): HistoryDao
}