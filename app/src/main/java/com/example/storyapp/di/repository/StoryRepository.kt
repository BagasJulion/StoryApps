package com.example.storyapp.di.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.map.MapsResponse
import com.example.storyapp.StoryRemoteMediator
import com.example.storyapp.database.StoryDatabase
import com.example.storyapp.login.LoginResponse
import com.example.storyapp.pref.AuthToken
import com.example.storyapp.login.LoginPreferences
import com.example.storyapp.register.RegisterResponse
import com.example.storyapp.Response.AddStoryResponse
import com.example.storyapp.Response.ErrorResponse
import com.example.storyapp.Response.ListStoryItem
import com.example.storyapp.Response.StoryResponse
import com.example.storyapp.result.Result
import com.example.storyapp.retrofit.api.ApiConfig
import com.example.storyapp.retrofit.api.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File


class StoryRepository private constructor(
    private val apiService: ApiService,
    private val preferences: LoginPreferences,
    private val storyDatabase: StoryDatabase
) {
    //Upload image
    fun uploadImage(imageFile: File, description: String) = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val user = runBlocking { preferences.getSession().first() }
            val response = ApiConfig.getApiService(user.token)
            val successResponse = response.addStory(multipartBody, requestBody)
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, AddStoryResponse::class.java)
            emit(Result.Error(errorResponse.message))
        }

    }

    //upload story with location
    fun getStoriesWithLocation(): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val getToken = preferences.getSession().first()
            val apiService = ApiConfig.getApiService(getToken.token)
            val locationStoryResponse = apiService.getStoriesWithLocation()
            emit(Result.Success(locationStoryResponse))
        } catch (e: HttpException) {
            val error = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(error, MapsResponse::class.java)
            val errorMessage = errorResponse?.message ?: "An error occurred"
            emit(Result.Error("Failed: $errorMessage"))
        } catch (e: Exception) {
            emit(Result.Error(e.toString()))
        }
    }

    //stories with paging
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return try {
            Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, preferences),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getAllStory()
                }
            ).liveData
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun saveSession(user: AuthToken){
        return preferences.saveSession(user)
    }

    fun getSession(): Flow<AuthToken> {
        return preferences.getSession()
    }

    suspend fun logout() {
        preferences.removeTokenAuth()
    }

    //register
    fun register(username: String, email: String, password: String): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val registerResponse = apiService.register(username, email, password)
            if (registerResponse.error == false) {
                emit(Result.Success(registerResponse))
            } else {
                emit(Result.Error(registerResponse.message ?: "Failed"))
            }
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody?.message ?: "Failed"
            emit(Result.Error("Registration Failed: ${e.message}"))
        }catch (e: Exception){
            emit(Result.Error("Internet Connection Issue"))
        }
    }

    //login
    fun login(email: String, password: String): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val responseLogin = apiService.login(email, password)
            if (responseLogin.error == false){
                val token = AuthToken(
                    token = responseLogin.loginResult?.token ?: "",
                    userId = responseLogin.loginResult?.userId ?: "",
                    name = responseLogin.loginResult?.name ?: "",
                    isLogin = true
                )
                ApiConfig.token = responseLogin.loginResult?.token ?: ""
                preferences.saveSession(token)
                emit(Result.Success(responseLogin))
            } else{
                emit(Result.Error(responseLogin.message ?: "Error"))
            }
        } catch (e : HttpException){
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody?.message ?: "Error"
            emit(Result.Error("Login Failed: $errorMessage"))
        }catch (e: Exception){
            emit(Result.Error("Internet Connection Issue"))
        }
    }


    companion object {
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, preference: LoginPreferences, storyDatabase: StoryDatabase
        ): StoryRepository {
            if (instance == null) {
                instance = StoryRepository(apiService, preference, storyDatabase)
            }
            return instance!!
        }
    }

}
