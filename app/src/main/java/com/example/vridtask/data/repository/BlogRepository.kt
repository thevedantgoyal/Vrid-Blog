package com.example.vridtask.data.repository

import android.util.Log
import com.example.vridtask.data.db.BlogDao
import com.example.vridtask.data.model.BlogPost
import com.example.vridtask.data.network.ApiService
import retrofit2.HttpException
import java.io.IOException

class BlogRepository(private val apiService: ApiService, private val blogDao: BlogDao)  {
    suspend fun getBlogPosts(page: Int, isOnline: Boolean): List<BlogPost> {
        val cachedPosts = blogDao.getBlogPosts()
        Log.d("BlogRepository", "Cached posts: ${cachedPosts.size}")
        if (!isOnline && cachedPosts.isNotEmpty()){
            Log.d("BlogRepository", "Returning cached posts (offline)")
            return cachedPosts
        }
        return try {
            val apiPosts = apiService.getBlogPosts(page = page)
            Log.d("BlogRepository", "API returned ${apiPosts.size} posts for page $page")

            if (apiPosts.isNotEmpty()) {
                if (page == 1) {
                    blogDao.clearBlogPosts()
                    Log.d("BlogRepository", "Cleared cache for page 1")
                }
                blogDao.insertBlogPosts(apiPosts)
                Log.d("BlogRepository", "Cached ${apiPosts.size} posts")
            }
            apiPosts
        } catch (e: IOException) {
            Log.e("BlogRepository", "IOException: ${e.message}")
            cachedPosts
        }catch (e : HttpException){
            Log.e("BlogRepository", "HttpException: ${e.message}")
            cachedPosts
        }

    }
}