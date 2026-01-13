package com.example.sproutly.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiInterface {

    // ---------- AUTH ----------
    @POST("login.php")
    suspend fun login(@Body body: LoginBody): Response<LoginResponse>

    // ---------- HOME ----------
    @GET("get_active_gardens.php")
    suspend fun getActiveGardens(): GardensResponse

    // ---------- GARDEN DETAIL ----------
    @GET("get_garden_detail.php")
    suspend fun getGardenDetail(
        @Query("gardenId") gardenId: Int,
        @Query("viewerEmail") viewerEmail: String
    ): Response<GardenDetailResponse>

    // ---------- CREATE GARDEN (foto opcional) ----------
    @Multipart
    @POST("create_garden.php")
    suspend fun createGarden(
        @Part("name") name: RequestBody,
        @Part("location") location: RequestBody,
        @Part("description") description: RequestBody,
        @Part("motivation") motivation: RequestBody,
        @Part("ownerEmail") ownerEmail: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<CreateGardenResponse>
}
