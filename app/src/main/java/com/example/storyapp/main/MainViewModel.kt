package com.example.storyapp.main


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.Response.ListStoryItem
import com.example.storyapp.di.repository.StoryRepository
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

val listStory: LiveData<PagingData<ListStoryItem>> =
    storyRepository.getStories().cachedIn(viewModelScope)
    fun getSession() = storyRepository.getSession().asLiveData()
    fun logout() {
        viewModelScope.launch {
            storyRepository.logout()
        }
    }
}
