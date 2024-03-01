package com.example.storyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityStoryDetailBinding

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val name = intent.getStringExtra(EXTRA_TITLE)
        val thumbnail = intent.getStringExtra(EXTRA_THUMBNAIL)
        val desc = intent.getStringExtra(EXTRA_DESC)

        binding.namePersonStory.text = name
        Glide.with(this).load(thumbnail).skipMemoryCache(true)
            .into(binding.imageViewStory)
        binding.descriptionStory.text = desc

    }

    companion object {
        const val EXTRA_THUMBNAIL = "extra_thumbnail"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESC = "extra_desc"

    }
}