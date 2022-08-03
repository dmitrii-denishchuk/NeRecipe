package ru.netology.nerecipe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nerecipe.data.ContentDao

@Database(
    entities = [ContentEntity::class],
    version = 1
)

abstract class ContentDb : RoomDatabase() {
    abstract val contentDao: ContentDao

    companion object {

        @Volatile
        private var instance: ContentDb? = null

        fun getInstance(context: Context): ContentDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context, ContentDb::class.java, "content.db"
        ).allowMainThreadQueries()
            .build()
    }
}