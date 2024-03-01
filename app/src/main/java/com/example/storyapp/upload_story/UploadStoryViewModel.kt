package com.example.storyapp.upload_story

import androidx.lifecycle.ViewModel
import com.example.storyapp.di.repository.StoryRepository
import java.io.File

class UploadStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun uploadImage(imageFile: File, description: String) = storyRepository.uploadImage(imageFile, description)
}