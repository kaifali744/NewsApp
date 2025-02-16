package com.example.newsapp.network

import com.example.newsapp.model.NewsResponse
import com.example.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun getNews(
        @Query("country") countryCode : String = "in",
        @Query("page") pageNumber : Int = 1,
        @Query("apiKey") apiKey : String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") searchQuery : String,
        @Query("page") pageNumber : Int = 1,
        @Query("apiKey") apiKey : String = API_KEY
    ): Response<NewsResponse>

}