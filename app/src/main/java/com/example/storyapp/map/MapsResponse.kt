package com.example.storyapp.map

import com.google.gson.annotations.SerializedName

data class MapsResponse (

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String
)