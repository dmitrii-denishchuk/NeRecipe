package ru.netology.nerecipe.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nerecipe.recipe.Step
import ru.netology.nerecipe.utils.ContentConverters

@Entity(tableName = "recipes")
class RecipeEntity(

    @PrimaryKey(autoGenerate = true)

    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean,

    @TypeConverters(ContentConverters::class)
    @ColumnInfo(name = "content")
    val step: List<Step>,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "picture")
    val picture: String,
)