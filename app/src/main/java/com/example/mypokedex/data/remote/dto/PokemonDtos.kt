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
    @Json(name = "height") val height: Int,  // en decímetros
    @Json(name = "weight") val weight: Int,  // en hectogramos
    @Json(name = "sprites") val sprites: SpritesDto,
    @Json(name = "types") val types: List<PokemonTypeSlotDto>,
    @Json(name = "stats") val stats: List<PokemonStatDto>
)

data class SpritesDto(
    @Json(name = "front_default") val front_default: String?,
    @Json(name = "other") val other: OtherSpritesDto? = null
)

data class OtherSpritesDto(
    @Json(name = "official-artwork") val officialArtwork: OfficialArtworkDto? = null
)

data class OfficialArtworkDto(
    @Json(name = "front_default") val front_default: String?
)

data class PokemonTypeSlotDto(
    @Json(name = "slot") val slot: Int,
    @Json(name = "type") val type: TypeDto
)

data class TypeDto(
    @Json(name = "name") val name: String
)

data class PokemonStatDto(
    @Json(name = "base_stat") val base_stat: Int,
    @Json(name = "stat") val stat: StatDto
)

data class StatDto(
    @Json(name = "name") val name: String
)