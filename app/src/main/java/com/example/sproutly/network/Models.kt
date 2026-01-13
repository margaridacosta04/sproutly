package com.example.sproutly.network

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    val status: String,
    val message: String? = null
)

data class LoginBody(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String? = null,
    val user: UserRemote? = null
)

data class UserRemote(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val score: Int = 0
)

data class GardenRemote(
    val id: Int,
    val name: String,
    val location: String,
    val description: String? = null,
    val ownerEmail: String? = null,
    val status: String? = null,
    val photoUrl: String? = null,
    val motivation: String? = null,
    val managerUserId: Int? = null
)

data class GardensResponse(
    val status: String,
    val gardens: List<GardenRemote> = emptyList(),
    val message: String? = null
)

data class GardenDetailResponse(
    val status: String? = null,
    val garden: GardenRemote,
    val members: List<UserRemote>? = null,
    val isMember: Boolean? = null,
    val message: String? = null
)

data class CreateGardenResponse(
    val status: String,
    val message: String? = null,
    val gardenId: Int? = null
)
