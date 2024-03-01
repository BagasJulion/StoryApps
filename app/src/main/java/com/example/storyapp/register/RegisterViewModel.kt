package com.example.storyapp.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.di.repository.StoryRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun register(username: String, email: String, password: String) = storyRepository.register(username, email, password)

    fun getSession() {
        viewModelScope.launch {
            storyRepository.getSession()
        }
    }
}