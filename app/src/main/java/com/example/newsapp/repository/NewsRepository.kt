package com.example.newsapp.repository

import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.Article
import com.example.newsapp.network.NewsApiService
import com.example.newsapp.network.RetrofitInstance

class NewsRepository(val db : ArticleDatabase) {

    suspend fun getNews(country : String, page : Int) =
        RetrofitInstance.api.getNews(country, page)

    suspend fun searchNews(searchQuery :String, page: Int) =
        RetrofitInstance.api.searchNews(searchQuery,page)

    suspend fun insertArticle(article: Article) =
        db.getArticleDao().insertArticle(article)

    suspend fun deleteArticle(article: Article) =
        db.getArticleDao().deleteArticle(article)

    fun getFavourites() = db.getArticleDao().getAllArticles()
}