package com.navin.storyapp.ui.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.navin.storyapp.api.ApiConfig
import com.navin.storyapp.api.StoryResponse
import com.navin.storyapp.helper.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryViewModel : ViewModel() {

    private val _storyList = MutableLiveData<StoryResponse>()
    val storyList: LiveData<StoryResponse> = _storyList

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isFailed = MutableLiveData<Event<Boolean>>()
    val isFailed: LiveData<Event<Boolean>> = _isFailed

    fun getStoryList(token: String) {
        _isLoading.value = Event(true)
        val service = ApiConfig().getApi().getAllStory("Bearer $token")
        service.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                _isLoading.value = Event(false)
                val storyList = response.body()?.listStory
                if (!storyList.isNullOrEmpty()) {
                    _storyList.value = response.body()
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _isFailed.value = Event(true)
                _isLoading.value = Event(false)
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    companion object {
        private const val TAG = "StoryViewModel"
    }
}