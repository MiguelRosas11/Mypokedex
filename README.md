
#Laboratorio 12- MyPokedex

**Repositorio:** [https://github.com/MiguelRosas11/Mypokedex/tree/Lab11](https://github.com/MiguelRosas11/Mypokedex/tree/Lab12)  
**Curso:** Programación de Plataformas Móviles – Universidad del Valle de Guatemala  


Cambia el tipo de orden, la lista se actualiza y mantiene esa configuración
al reiniciar (puedes forzar el cierre y volver a abrir):




https://github.com/user-attachments/assets/ce69014f-1224-4bcc-ae39-385bd1fb85f4


Abre la App sin conexión, se muestran los Pokémon vistos previamente.:



https://github.com/user-attachments/assets/fd999d17-994d-48b0-b7d3-631a80997e5b


Al recuperar conexión, los datos se actualizan correctamente:

al principio se ve asi:

<img width="401" height="893" alt="image" src="https://github.com/user-attachments/assets/27cc12fb-1d7c-4fad-a708-df96781edac0" />



pero luego de activar el internet tarda un poco en cargar por eso no incluimos gift pero si carga y llega a :


<img width="420" height="917" alt="image" src="https://github.com/user-attachments/assets/f27fcaeb-4a21-4e5e-b625-938e1f650e9b" />








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







## Laboratorio #9 — MVVM con Jetpack Compose  
**Curso:** Programación de Plataformas Móviles  
**Universidad del Valle de Guatemala**

---

### Contexto y Objetivo

En Android moderno, la interfaz de usuario (UI) se construye como una **función del estado**. Jetpack Compose facilita este enfoque declarativo, pero el manejo del estado a través del ciclo de vida (rotaciones, navegación, recreaciones) requiere una arquitectura que lo preserve y lo organice correctamente.

El objetivo del laboratorio fue **construir una mini aplicación de “Lista de Deseos”** (Wishlist App) en tres partes para comprender los límites del estado local (`remember`), implementar **MVVM** con `ViewModel` + `StateFlow`, y finalmente compartir el mismo estado entre pantallas usando **Navigation Compose**.

---

## Parte 1 — Estado local y sus límites

**Archivo:** `LikeCounterScreen.kt`

Se construyó una pantalla sencilla con un contador de "Me Gusta" que incrementa con un botón:

```kotlin
var likeCount by remember { mutableStateOf(0) }
```

Al rotar el dispositivo, el contador vuelve a cero porque el estado se almacena únicamente en memoria temporal (local al Composable).

### Reflexión 1 
El estado no persiste porque **`remember`** guarda los datos dentro de la composición activa.  
Cuando la actividad se destruye por una rotación, se crea una nueva instancia de la UI, perdiendo todo el estado previo.  
Esto muestra la necesidad de una fuente de verdad más duradera: el **ViewModel**.

<img width="1918" height="1078" alt="LikeCounterScreen" src="https://github.com/user-attachments/assets/1b749bc4-b8bd-443d-9e44-2a860672bd76" />


---

## Parte 2 — MVVM con ViewModel y StateFlow

**Archivos clave:**
- `Product.kt`
- `WishlistUiState.kt`
- `WishlistViewModel.kt`
- `WishlistScreen.kt`

Se refactorizó el proyecto usando el patrón **MVVM** (Model–View–ViewModel):

- `WishlistViewModel` mantiene el estado de la UI con un `MutableStateFlow`.
- La UI (`WishlistScreen`) recolecta el flujo con `collectAsStateWithLifecycle()`.
- Los productos se representan como una lista de `Product` y se pueden marcar como favoritos con un ícono de estrella.

```kotlin
val uiState = vm.uiState.collectAsStateWithLifecycle().value
```

El estado persiste al rotar el dispositivo gracias al **ViewModel**, que vive mientras la actividad o el gráfico de navegación exista.

### Reflexión 2  
El ViewModel resuelve el problema anterior porque funciona como **única fuente de verdad** para la UI y sobrevive a los cambios de configuración.  
Además, al usar `StateFlow`, el flujo de datos es reactivo y seguro para el ciclo de vida.

<img width="1918" height="1078" alt="WishlistScreen" src="https://github.com/user-attachments/assets/b0a992b9-65ee-473b-8286-a58bfbf5ad6e" />


---

## Parte 3 — Navigation Compose y ViewModel compartido

**Archivos clave:**
- `AppNavGraph.kt`
- `Destinations.kt`
- `ProfileScreen.kt`

Se configuró un **NavHost** con dos destinos:
- `WishlistScreen` (lista principal)
- `ProfileScreen` (contador de favoritos)

Ambas pantallas comparten el mismo `WishlistViewModel`, creado con alcance del gráfico raíz:

```kotlin
val parentEntry = remember(backStackEntry) {
    navController.getBackStackEntry(Destinations.Root.route)
}
val vm: WishlistViewModel = viewModel(parentEntry)
```

Esto permite que las dos pantallas accedan al mismo estado sin duplicarlo.

### Reflexión 3  
Compartir un `ViewModel` entre destinos es más eficiente y limpio que pasar listas o argumentos por navegación.  
Se mantiene una **única fuente de verdad**, evitando inconsistencias y garantizando sincronización entre pantallas.

<img width="1918" height="1078" alt="ProfileScreen" src="https://github.com/user-attachments/assets/b2ca91f4-6cb2-464b-a694-69e03c5e73dd" />


---

## Conceptos aplicados
- **MVVM:** separación clara entre lógica (ViewModel), datos (Model) y presentación (UI).  
- **StateFlow + collectAsStateWithLifecycle():** flujo de datos reactivo y seguro.  
- **Navigation Compose:** manejo estructurado de destinos y back stack.  
- **ViewModel compartido:** persistencia del estado entre pantallas.

---

## Conclusión
Este laboratorio demostró el poder del enfoque declarativo de Compose y la importancia del **manejo de estado a nivel de arquitectura**.  
`remember` es útil para estados efímeros, pero **MVVM con ViewModel y StateFlow** permite construir aplicaciones robustas, escalables y reactivas.  
La integración con **Navigation Compose** consolida un flujo unidireccional de datos (UDF) limpio y sostenible.



