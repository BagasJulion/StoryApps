package com.example.storyapp.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.LoadingStateAdapter
import com.example.storyapp.map.MapsActivity
import com.example.storyapp.R
import com.example.storyapp.StoryPagingAdapter
import com.example.storyapp.upload_story.UploadStoryActivity
import com.example.storyapp.WelcomeActivity
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private val storyPagingAdapter = StoryPagingAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }



        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, UploadStoryActivity::class.java))
        }


        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager

        val item = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStory.addItemDecoration(item)

        viewModel.getSession().observe(this){ session ->
            if ( !session.isLogin){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        binding.rvStory.adapter = storyPagingAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter{
                storyPagingAdapter.retry()
            }
        )


        viewModel.listStory.observe(this){story ->
            storyPagingAdapter.submitData(lifecycle, story)
            showLoading(false)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                viewModel.logout()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                true
            }

            R.id.maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}


