package com.example.vridtask.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vridtask.data.model.BlogPost

@Dao
interface BlogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlogPosts(posts: List<BlogPost>)

    @Query("SELECT * FROM blog_posts ORDER BY id DESC")
    suspend fun getBlogPosts(): List<BlogPost>

    @Query("DELETE FROM blog_posts")
    suspend fun clearBlogPosts()
}

