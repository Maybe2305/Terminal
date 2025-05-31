package com.may.terminal.data

import retrofit2.http.GET

interface ApiService {

    @GET("aggs/ticker/AAPL/range/1/hour/2022-01-09/2024-01-09?adjusted=true&sort=desc&limit=50000&apiKey=4JI7R6aLC32f2zppfircHLznqqbb1NVM")
    suspend fun loadBars(): Result
}