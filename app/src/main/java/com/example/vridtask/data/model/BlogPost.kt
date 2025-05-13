package com.example.vridtask.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "blog_posts")
data class BlogPost (
    @PrimaryKey val id: Int,

    @Embedded(prefix = "title_")
    val title: Title,


    @SerializedName("link")
    val link: String? = null,

    val date: String? = null,
    val slug: String? = null

) {
    data class Title(
        @SerializedName("rendered")
        val rendered: String
    )
}