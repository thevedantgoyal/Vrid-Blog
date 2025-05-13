package com.example.vridtask.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vridtask.data.model.BlogPost
import com.example.vridtask.data.repository.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class BlogViewModel(private val repository: BlogRepository) : ViewModel(){


    private val _blogPosts = MutableStateFlow<List<BlogPost>>(emptyList())
    val blogPosts: StateFlow<List<BlogPost>> = _blogPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 1

    init {
        fetchBlogPosts(true)
    }

     fun fetchBlogPosts(isOnline : Boolean){
        if (_isLoading.value) {
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val newPosts = repository.getBlogPosts(currentPage, isOnline)

                if (newPosts.isNotEmpty()) {
                    val currentPosts = if (currentPage == 1) emptyList() else _blogPosts.value
                    _blogPosts.value = currentPosts + newPosts
                    currentPage++
                    Log.d("BlogViewModel", "Updated posts: total now ${_blogPosts.value.size}")
                } else {
                    Log.d("BlogViewModel", "No new posts received")
                }
            }catch (e : IOException){
                Log.e("BlogViewModel", "Error fetching posts: ${e.message}")
            }
            finally {
                _isLoading.value = false
            }
        }
    }
    fun refreshPosts(isOnline: Boolean) {
        currentPage = 1
        _blogPosts.value = emptyList()
        fetchBlogPosts(isOnline)
    }
}