package com.example.mypokedex.data.remote.dto

import com.squareup.moshi.Json

data class PokemonListDto(
    @Json(name = "count") val count: Int,
    @Json(name = "next") val next: String?,
    @Json(name = "previous") val previous: String?,
    @Json(name = "results") val results: List<PokemonEntryDto>
)

data class PokemonEntryDto(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

data class PokemonDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "sprites") val sprites: Sprites
)

data class Sprites(
    @Json(name = "front_default") val front_default: String?
)


