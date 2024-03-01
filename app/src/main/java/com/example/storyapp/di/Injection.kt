package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.database.StoryDatabase
import com.example.storyapp.login.LoginPreferences
import com.example.storyapp.retrofit.api.ApiConfig
import com.example.storyapp.login.dataStore
import com.example.storyapp.di.repository.StoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = LoginPreferences.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val storyDatabase = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService(user.token)
        return StoryRepository.getInstance(apiService, pref, storyDatabase)
    }
}