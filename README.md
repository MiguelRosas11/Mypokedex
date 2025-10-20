# Laboratorio #11 – MyPokedex  
**Repositorio:** [https://github.com/MiguelRosas11/Mypokedex/tree/Lab11](https://github.com/MiguelRosas11/Mypokedex/tree/Lab11)  
**Curso:** Programación de Plataformas Móviles – Universidad del Valle de Guatemala  
**Arquitectura:** MVVM + Jetpack Compose + PokeAPI  

## Objetivo  
Desarrollar una aplicación móvil llamada MyPokedex que funcione como Pokédex, consumiendo datos de la PokeAPI y aplicando la arquitectura MVVM para separar las capas de presentación, lógica de negocio y datos. Se incluye paginación, manejo de estados (cargando, éxito, error) y visualización del detalle de cada Pokémon.

## Arquitectura (MVVM)  
El patrón MVVM se implementó separando las responsabilidades en tres capas:  
- **Model / Data layer:** Contiene los modelos de datos y el repositorio que obtiene la información desde la API.  
- **ViewModel layer:** Expone los estados de la interfaz (UiState) y gestiona los eventos como carga, búsqueda o error.  
- **View / UI layer:** Implementada con Jetpack Compose. Las pantallas observan los estados del ViewModel y reaccionan ante los cambios.  

## Funcionalidades Principales  
- Listado de Pokémon con scroll infinito (paginación).

https://github.com/user-attachments/assets/a79370c1-1d24-437d-a5b5-e542f9c66a2a

- Manejo de estados de carga y error.
![WhatsApp Image 2025-10-19 at 11 48 20 PM](https://github.com/user-attachments/assets/903c37f9-3f76-4486-a02a-69ff54b71eee)




## Conclusiones  
- Se logró consumir datos reales desde la PokeAPI y mostrarlos en una aplicación funcional.  
- La arquitectura MVVM permitió mantener una buena separación de responsabilidades.  
- El uso de corrutinas y Flow mejoró la fluidez y el manejo de estados en la UI.  
- Este laboratorio permitió reforzar el manejo de asincronía y consumo de APIs en Compose.

