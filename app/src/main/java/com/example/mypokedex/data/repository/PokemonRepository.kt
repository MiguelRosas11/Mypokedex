package com.example.mypokedex.data.repository

import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.toPokemon
import com.example.mypokedex.data.remote.PokeApi
import retrofit2.HttpException
import java.io.IOException

class PokemonRepository(private val api: PokeApi) {
    private val pageSize = 30

    suspend fun getPage(pageIndex: Int): Result<List<Pokemon>> = try {
        val offset = pageIndex * pageSize
        val dto = api.getPokemonPage(limit = pageSize, offset = offset)
        Result.Success(dto.results.map { it.toPokemon() })
    } catch (_: IOException) {
        Result.Error("Sin conexión. Reintenta.")
    } catch (e: HttpException) {
        Result.Error("Error ${e.code()}.")
    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error("Error inesperado: ${e.localizedMessage ?: e.javaClass.simpleName}")
    }

    suspend fun getDetail(idOrName: String): Result<Pokemon> = try {
        Result.Success(api.getPokemonDetail(idOrName).toPokemon())
    } catch (_: IOException) {
        Result.Error("Sin conexión. Reintenta.")
    } catch (_: HttpException) {
        Result.Error("No se encontró el Pokémon.")
    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error("Error inesperado: ${e.localizedMessage ?: e.javaClass.simpleName}")
    }
}