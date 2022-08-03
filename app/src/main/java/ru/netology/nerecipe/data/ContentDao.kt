package ru.netology.nerecipe.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nerecipe.db.ContentEntity

@Dao
interface ContentDao {

    @Query("SELECT * FROM recipes_content ORDER BY id DESC")
    fun getAll(): LiveData<List<ContentEntity>>

    @Insert
    fun insert(content: ContentEntity)

    @Query(
        """
        UPDATE recipes_content SET
        content = :content,
        picture = :picture
        WHERE id = :id;
        """
    )
    fun update(id: Long, content: String, picture: String)

    fun save(content: ContentEntity) =
        if (content.id == 0L) insert(content) else update(content.id, content.content, content.picture)

    @Query("DELETE FROM recipes_content WHERE id = :id")
    fun removeById(id: Long)
}