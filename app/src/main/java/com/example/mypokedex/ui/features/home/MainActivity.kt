package com.example.mypokedex.ui.features.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mypokedex.ui.features.home.HomeScreen
import com.example.mypokedex.ui.theme.MypokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MypokedexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MypokedexTheme {
        HomeScreen()
    }
}