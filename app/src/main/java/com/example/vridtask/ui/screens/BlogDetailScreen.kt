package com.example.vridtask.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController


@Composable
fun BlogDetailScreen(link: String, navController: NavController){
    AndroidView( factory = {
            context -> WebView(context).apply {
        loadUrl(link)
    }
  }, modifier = androidx.compose.ui.Modifier.fillMaxSize())
}
