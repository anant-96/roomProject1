package com.example.roomproject1.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriber_data_table")
data class Subscriber(
    @ColumnInfo(name = "subscriber_name")
    var name : String,

    @ColumnInfo(name = "subscriber_email")
    var email : String,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subscriber_id")
    val id : Int = 0
)
