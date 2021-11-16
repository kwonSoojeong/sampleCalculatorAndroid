package com.crystal.samplecalculator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History (
    @PrimaryKey val uid: Int?,
    @ColumnInfo val expression: String?,
    @ColumnInfo val result: String?
)