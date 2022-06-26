package com.navin.storyapp.ui.main

import androidx.activity.viewModels
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.navin.storyapp.ViewModelFactory
import com.navin.storyapp.api.StoryResponse
import com.navin.storyapp.model.UserPreference
import com.navin.storyapp.ui.story.AddStoryActivity
import com.navin.storyapp.ui.login.LoginActivity
import com.navin.storyapp.ui.story.StoryAdapter
import com.navin.storyapp.ui.story.StoryViewModel
import com.navin.storyapp.ui.splash.SplashViewModel
import com.navin.storyapp.R
import com.navin.storyapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var storyAdapter: StoryAdapter
//    private val splashViewModel: SplashViewModel by viewModels()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        installSplashScreen().apply {
//            setKeepOnScreenCondition {
//                splashViewModel.isLoading.value
//            }
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()

        storyAdapter = StoryAdapter()

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.setHasFixedSize(true)
        binding.rvStory.adapter = storyAdapter

        binding.addStory.setOnClickListener { view ->
            val intent = Intent(this, AddStoryActivity::class.java)

            startActivity(
                intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
            )
        }
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (user.token.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                storyViewModel.getStoryList(user.token)
            }
        }

        storyViewModel.storyList.observe(this) {
            setStoryList(it)
        }

        storyViewModel.isLoading.observe(this) {
            it.getContentIfNotHandled()?.let { state ->
                isLoading(state)
            }
        }

        storyViewModel.isFailed.observe(this) {
            it.getContentIfNotHandled()?.let { state ->
                isFailed(state)
            }
        }
    }


    private fun setStoryList(storyList: StoryResponse) {
        storyAdapter.setListStory(storyList.listStory)
    }

    private fun Logout() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.title_info))
            setMessage(getString(R.string.message_logout))
            setPositiveButton(getString(R.string.yes_)) { _, _ ->
                mainViewModel.logout()
            }
            setNegativeButton(getString(R.string.cancel_)) { dialog, _ -> dialog.cancel() }
            create()
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.languange -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.logout -> {
                Logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isLoading(loading: Boolean) {
        if (loading) {
            binding.progressBar4.visibility = View.VISIBLE
            binding.rvStory.visibility = View.INVISIBLE
        } else {
            binding.progressBar4.visibility = View.INVISIBLE
            binding.rvStory.visibility = View.VISIBLE
        }
    }

    private fun isFailed(failed: Boolean) {
        if (failed) {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.title_warning))
                setCancelable(false)
                setMessage(getString(R.string.message_conection))
                setPositiveButton(getString(R.string.ok_)) { _, _ ->
                }
                create()
                show()
            }
        }
    }

    companion object {
        const val KEY_STORY = "story"

    }
}