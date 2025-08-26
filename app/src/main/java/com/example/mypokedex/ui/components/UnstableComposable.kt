package com.example.mypokedex.ui.components

// ui/components/UnstableComposable.kt
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import kotlin.random.Random

// ¿PREGUNTAS POR HACERSE?
// 1. ¿No es idempotente?
// 2. ¿Tiene efectos secundarios?
// 3. ¿Usa un tipo de dato inestable (List) como parámetro?

@Composable
fun UnstablePokemonList(pokemons: List<String>){
    val randomColor = remember {
        String.format("#%06x", Random.nextInt(0, 0xFFFFFF))
    }
    SideEffect {
        println("Composing StablePokemonList with color $randomColor")
    }

    Button(onClick = { /* no hace nada, solo para forzar recomposición */ }) {
        Text(text = "Tengo ${pokemons.size} Pokémon favoritos")
    }
}