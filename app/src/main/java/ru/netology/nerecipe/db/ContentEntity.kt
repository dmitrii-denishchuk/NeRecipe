package ru.netology.nerecipe.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes_content")
class ContentEntity(

    @PrimaryKey(autoGenerate = true)

    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "picture")
    val picture: String,
)