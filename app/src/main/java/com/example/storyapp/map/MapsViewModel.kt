package com.example.storyapp.map

import androidx.lifecycle.ViewModel
import com.example.storyapp.di.repository.StoryRepository


class MapsViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun getStoryLocation() = storyRepository.getStoriesWithLocation()

}