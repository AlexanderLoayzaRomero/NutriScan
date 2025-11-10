# NutriScan 🥑

NutriScan es una aplicación de seguimiento nutricional para Android, desarrollada como un proyecto final. Permite a los usuarios registrar sus comidas, escanear productos y realizar un seguimiento de su ingesta calórica diaria contra un objetivo personalizado y calculado científicamente.

La aplicación está construida enteramente con tecnologías modernas de Android, incluyendo Jetpack Compose para la UI, Room para la base de datos local, Retrofit para el consumo de API, y DataStore para la persistencia de preferencias.

## ✨ Características Principales

* **Dashboard Dinámico:** Una pantalla de inicio que muestra un resumen de las calorías del día con un indicador de progreso circular contra una meta personalizada.
* **Meta Calórica Inteligente:** Calcula automáticamente la meta de ingesta calórica del usuario basándose en su perfil (peso, altura, edad, género), nivel de actividad y objetivo (bajar, mantener o subir de peso) usando la fórmula de Mifflin-St Jeor.
* **Gestión Completa (CRUD):** Los usuarios pueden **C**rear, **L**eer, **A**ctualizar y **B**orrar (CRUD) todas sus entradas de alimentos.
* **Escáner de Código de Barras:** Utiliza ML Kit para escanear códigos de barras de productos y obtener automáticamente la información nutricional desde la API de OpenFoodFacts.
* **Búsqueda de Alimentos:** Búsqueda "mientras escribes" (search-as-you-type) que consume la API de OpenFoodFacts para encontrar datos nutricionales.
* **Registro Fotográfico:** Permite a los usuarios tomar una foto con la cámara y adjuntarla a cualquier entrada de comida.
* **Historial Detallado:** Un "Registro Alimenticio" que muestra todas las comidas agrupadas por fecha ("Hoy", "Ayer", "9 de noviembre", etc.).
* **Deslizar para Borrar:** Funcionalidad intuitiva de `SwipeToDismiss` en todas las listas de alimentos para una fácil eliminación.

## 🛠️ Stack Tecnológico

* **UI:** 100% Jetpack Compose
* **Diseño:** Material 3
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Programación Asíncrona:** Kotlin Coroutines & Flow
* **Base de Datos Local:** Room (con migraciones de BD)
* **Red (Networking):** Retrofit & Kotlinx.Serialization
* **Guardado de Preferencias:** Jetpack DataStore
* **Carga de Imágenes:** Coil
* **Hardware/APIs de Google:**
    * ML Kit Barcode Scanning
    * `FileProvider` & Activity Result Contracts (para la Cámara)

