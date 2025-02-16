package com.example.newsapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.newsapp.db.Converter
import java.io.Serializable

@Entity(tableName = "articles")
data class Article(

    @PrimaryKey(autoGenerate = true)
    var id : Int? = null,
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    @TypeConverters(Converter::class)
    val source: Source,
    val title: String,
    val url: String,
    val urlToImage: String
):Serializable