package com.utils.cloud

import com.model.cloud.CloudTracks
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface
{
    @Headers("X-RapidAPI-Key: 53f9ece60bmshb722e6fc81d7adcp159468jsnfbc79735fb2f",
             "X-RapidAPI-Host: deezerdevs-deezer.p.rapidapi.com")
    @GET("search")
    fun getData(@Query("q") query: String) : Call<CloudTracks>
}