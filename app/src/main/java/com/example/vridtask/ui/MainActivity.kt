package com.example.vridtask.ui

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vridtask.data.db.BlogDatabase
import com.example.vridtask.data.network.RetrofitClient
import com.example.vridtask.data.repository.BlogRepository
import com.example.vridtask.ui.screens.BlogDetailScreen
import com.example.vridtask.ui.screens.BlogListScreen
import com.example.vridtask.ui.theme.VridTaskTheme
import com.example.vridtask.viewModel.BlogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        testApiDirectly()
        setContent {
            VridTaskTheme {
                BlogApp()
            }
        }
    }

@Composable
fun BlogApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        val database = BlogDatabase.getDatabase(LocalContext.current)
        val repository = BlogRepository(RetrofitClient.apiService, database.blogDao())
        val viewModel: BlogViewModel = viewModel(factory = BlogViewModelFactory(repository))
        NavHost(navController = navController, startDestination = "blog_list") {
            composable("blog_list") {
                BlogListScreen(viewModel = viewModel, navController = navController)
            }
            composable(
                route = "blog_detail/{link}",
                arguments = listOf(navArgument("link") { type = NavType.StringType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("link") ?: ""
                val decodedLink = try {
                    String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP))
                } catch (e: Exception) {
                    Log.e("BlogDetail", "Failed to decode link: $encoded", e)
                    ""
                }
                BlogDetailScreen(link = decodedLink, navController = navController)
            }
        }
    }
}

class BlogViewModelFactory(private val repository: BlogRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

    private fun testApiDirectly() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MainActivity", "Testing API directly...")
                val response = RetrofitClient.apiService.getBlogPosts(page = 1)
                Log.d("MainActivity", "API Success: ${response.size} posts received")
                response.forEachIndexed { index, post ->
                    Log.d("MainActivity", "Post $index: ID=${post.id}, Title=${post.title.rendered}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "API Error: ${e.message}", e)
            }
        }
    }
}
