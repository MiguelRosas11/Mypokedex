package com.example.mypokedex.ui.features.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import com.example.mypokedex.ui.theme.MypokedexTheme
import com.example.mypokedex.ui.features.home.HomeScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MypokedexTheme {
                Scaffold {
                    HomeScreen()
                }
            }
        }
    }
}