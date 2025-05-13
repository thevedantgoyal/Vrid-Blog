package com.example.vridtask.data.network

import com.example.vridtask.data.model.BlogPost
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("wp-json/wp/v2/posts")
    suspend fun getBlogPosts(
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int ): List<BlogPost>
}

