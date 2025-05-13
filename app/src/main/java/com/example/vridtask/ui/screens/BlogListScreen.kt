package com.example.vridtask.ui.screens

import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vridtask.data.model.BlogPost
import com.example.vridtask.viewModel.BlogViewModel
import android.util.Base64
import android.widget.Toast
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogListScreen(viewModel: BlogViewModel, navController: NavController) {
    val blogPosts by viewModel.blogPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val isOnline = connectivityManager.activeNetwork != null


    val reachedEnd by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= blogPosts.size - 2 && blogPosts.isNotEmpty()
        }
    }


    LaunchedEffect(reachedEnd) {
        if (reachedEnd && !isLoading) {
            Log.d("BlogListScreen", "Reached end of list, loading more posts")
            viewModel.fetchBlogPosts(isOnline)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Vrid Blog") },
                actions = {
                    IconButton(onClick = {
                        Log.d("BlogListScreen", "Refresh clicked")
                        viewModel.refreshPosts(isOnline)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )

            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                if (blogPosts.isEmpty() && !isLoading) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isOnline) "No posts available" else "Offline: No cached posts",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            Log.d("BlogListScreen", "Try again clicked")
                            viewModel.refreshPosts(isOnline)
                        }) {
                            Text("Try Again")
                        }

                        // Debug info
                        if (!isOnline) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Debug: You're offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    // List of posts
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = blogPosts,
                            key = { post -> post.id }
                        ) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Log.d("BlogListScreen", "Post clicked: ${post.id}, navigating to ${post.link}")

                                        val link = post.link
                                        if (link != null) {
                                            try {
                                                val encodedLink = Base64.encodeToString(link.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
                                                navController.navigate("blog_detail/$encodedLink")
                                            } catch (e: Exception) {
                                                Log.e("BlogListScreen", "Navigation failed: ${e.message}", e)
                                            }
                                        } else {
                                            Log.e("BlogListScreen", "Link is null, cannot navigate")
                                            Toast.makeText(context, "Link is null, cannot open web view", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = post.title.rendered,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    // Optional: Show post date if available
                                    post.date?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Debug overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Posts: ${blogPosts.size}",
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "Online: $isOnline",
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "Loading: $isLoading",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}