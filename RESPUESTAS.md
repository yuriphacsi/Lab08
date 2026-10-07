# Laboratorio 08 — Arquitectura MVVM y UDF con persistencia

## 1. Configuración del proyecto

Proyecto: Lab08. Lenguaje: Kotlin. Interfaz: Jetpack Compose. Persistencia: Room 2.6.1 con Kapt.

El proyecto contiene las dependencias del documento. La versión de Lifecycle se declara por separado, aunque en este laboratorio coincide con Room: son bibliotecas independientes. Se utiliza JDK 17, Gradle 8.7, Android Gradle Plugin 8.5.2 y SDK 34 para mantener una configuración definida y compatible con el ejemplo de Room 2.6.1.

## 2. Arquitectura MVVM y UDF

El modelo está formado por Task, TaskDao y TaskDatabase. TaskViewModel recibe las acciones, trabaja con el DAO y publica la lista mediante StateFlow. TaskScreen muestra ese estado y envía eventos al ViewModel. La vista no ejecuta consultas SQL. Este recorrido mantiene el flujo unidireccional: evento de la vista, cambio en el ViewModel y nuevo estado para la vista.

## 3. Implementación del modelo y base de datos

### Paso 1. Tipos y configuraciones del modelo

Room relaciona las propiedades de Kotlin con columnas de SQLite. Para este proyecto, id identifica cada tarea, description guarda el texto e isCompleted representa su estado.

| Tipo Kotlin | Almacenamiento habitual en SQLite | Uso |
| --- | --- | --- |
| Int, Long, Short y Byte | INTEGER | Identificadores y cantidades |
| Boolean | INTEGER, 0 o 1 | Pendiente o completada |
| Float y Double | REAL | Valores decimales |
| String | TEXT | Descripción |
| ByteArray | BLOB | Datos binarios |
| Tipo nullable, como String? | Admite NULL | Dato opcional |

Configuraciones: @Entity(tableName) define la tabla; @PrimaryKey(autoGenerate = true) genera identificadores; primaryKeys permite una clave compuesta; @ColumnInfo(name) cambia el nombre de una columna; @Ignore excluye una propiedad; indices y unique permiten índices y restricciones de unicidad. Un valor inicial de Kotlin no equivale automáticamente a un DEFAULT de SQL: para este último existe @ColumnInfo(defaultValue).

En Room 2.6.1, los tipos personalizados pueden convertirse con @TypeConverter y registrarse con @TypeConverters. Por ejemplo, una fecha puede transformarse a Long y recuperarse después. @Embedded agrupa columnas de un objeto y ForeignKey establece integridad referencial entre tablas. Si cambia el esquema, se debe aumentar la versión y definir la migración correspondiente.

Fuentes:
- https://developer.android.com/training/data-storage/room/defining-data
- https://developer.android.com/reference/androidx/room/ColumnInfo
- https://developer.android.com/reference/androidx/room/TypeConverter
- https://developer.android.com/reference/androidx/room/Embedded
- https://developer.android.com/reference/androidx/room/ForeignKey

### Paso 2. ¿Por qué usamos suspend?

Suspend permite que una función suspenda y reanude su ejecución dentro de una corrutina sin bloquear el hilo mientras espera. En este DAO, Room implementa las operaciones suspendibles de base de datos de forma asíncrona. Las llamamos con viewModelScope.launch. Suspend por sí solo no crea un hilo ni convierte cualquier código bloqueante en trabajo de fondo; el soporte asíncrono de Room es el que permite estas consultas sin bloquear la interfaz. Las operaciones que devuelven Flow pueden declararse sin suspend porque la consulta se realiza al recoger el flujo.

Fuente: https://developer.android.com/training/data-storage/room/async-queries

### Proyecto real con un DAO de varias funciones

Proyecto: android/architecture-samples, ejemplo público de Android.

Archivo consultado:
https://github.com/android/architecture-samples/blob/main/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/local/TaskDao.kt

El archivo incluye observeAll y observeById, que devuelven Flow para observar cambios; getAll y getById, para consultas puntuales; upsert y upsertAll, para insertar o actualizar; updateCompleted, para modificar solo el estado; y deleteById, deleteAll y deleteCompleted, para distintos tipos de eliminación.

Me parece útil que getById retorne LocalTask?, porque puede no existir el registro. También destaca que deleteById y deleteCompleted devuelvan Int: así se conoce cuántas filas se eliminaron. Los parámetros con nombre, como :taskId, permiten vincular valores sin concatenarlos al SQL. La combinación de Flow y consultas suspendibles muestra la diferencia entre observar cambios y solicitar un resultado una sola vez.

### Paso 3. Base de datos

TaskDatabase declara Task como entidad, utiliza la versión 1 y expone taskDao(). La base se llama task_db. Se conserva una instancia compartida usando applicationContext para evitar construir bases nuevas durante las recomposiciones. No se utiliza allowMainThreadQueries.

## 4. ViewModel e investigación de LiveData

LiveData mantiene datos observables y considera el ciclo de vida de sus observadores. En MVVM, el ViewModel puede guardar un MutableLiveData privado y exponer un LiveData de solo lectura. La vista recibe sus cambios sin modificar directamente el estado.

Ejemplo equivalente para cargar y agregar tareas —solo ilustrativo, no reemplaza el StateFlow utilizado en la app—:

```kotlin
class TaskLiveDataViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    fun addTask(description: String) {
        val text = description.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            dao.insertTask(Task(description = text))
            _tasks.value = dao.getAllTasks()
        }
    }
}
```

Requiere los imports ViewModel, LiveData, MutableLiveData y viewModelScope de androidx.lifecycle y launch de kotlinx.coroutines. En Compose se usaría `val tasks by viewModel.tasks.observeAsState(emptyList())`, con el import androidx.compose.runtime.livedata.observeAsState y la dependencia androidx.compose.runtime:runtime-livedata. Las clases LiveData están en lifecycle-livedata-ktx. Estas dependencias solo serían necesarias al aplicar la alternativa; no se añadieron a la app porque el laboratorio trabaja con StateFlow.

| Aspecto | LiveData | StateFlow |
| --- | --- | --- |
| Valor inicial | Puede no tenerlo | Es obligatorio |
| Ciclo de vida | observe(owner) lo considera | La recolección debe gestionarlo |
| Compose | observeAsState | collectAsStateWithLifecycle |
| Operadores | Transformaciones de LiveData | Operadores de Flow y corrutinas |
| Ámbito | Biblioteca de Android | Kotlin Coroutines |

Al cambiar a LiveData, las operaciones del DAO podrían mantenerse. Cambiarían el contenedor del estado y la forma de observarlo. En ambos casos, la UI debe enviar acciones al ViewModel y recibir su estado. LiveData no almacena los registros de forma persistente: esa tarea sigue siendo de Room.

Fuentes y ejemplos:
- https://developer.android.com/topic/libraries/architecture/livedata
- https://developer.android.com/kotlin/flow/stateflow-and-sharedflow

## 5. Implementación de la vista

La versión guiada permite agregar una tarea, alternar su estado y eliminar todas las tareas. La lista se obtiene desde StateFlow y se actualiza después de cada operación de Room. Se usa viewModel(factory = ...) para conservar correctamente el ViewModel durante cambios de configuración. La lista usa LazyColumn para permitir desplazamiento.

Evidencia de funcionamiento: pendiente de capturar al ejecutar en un dispositivo o emulador. Consulta los pasos de INSTRUCCIONES.md.

Repositorio remoto: pendiente de publicar desde la cuenta del alumno. El ZIP contiene el historial local de avances, con autor Codex, y las instrucciones para subirlo sin perder los commits.

## Ejercicio 1

Diseño de referencia: Microsoft To Do.
https://www.microsoft.com/en-us/microsoft-365/microsoft-to-do-list-app

La vista utiliza una cabecera azul, una lista de tarjetas claras y casillas para marcar las tareas, tomando esos elementos como inspiración visual. No reproduce todos los controles de la aplicación original.

Las dos características seleccionadas son:

1. Editar tareas: el botón Editar abre un diálogo con la descripción actual; Guardar envía el cambio al ViewModel y este usa @Update. Se conservan el identificador y el estado de la tarea.
2. Eliminar tareas individualmente: el botón Eliminar envía el identificador al DAO y ejecuta DELETE con WHERE id = :id. Las otras tareas permanecen.

Se mantienen las funciones de la parte guiada. No se implementaron otras características de la lista del ejercicio.

## Puntos adicionales

Los dos módulos adicionales de Room pertenecen al apartado de bonificación. No están desarrollados en esta entrega.

## Observaciones

1. Room necesita que el compilador de anotaciones se configure mediante Kapt; agregar solamente room-runtime no genera la implementación de la base de datos.
2. StateFlow mantiene el estado que ve la interfaz, pero Room es quien guarda las tareas para recuperarlas después de cerrar la aplicación.
3. Crear el ViewModel directamente dentro de setContent puede producir instancias nuevas. Utilizar una Factory con viewModel() permite vincularlo al ciclo de vida correspondiente.

## Conclusiones

1. Considero que separar modelo, vista y ViewModel facilita ubicar dónde se procesa cada acción y evita mezclar consultas con los componentes de pantalla.
2. La documentación permite distinguir que suspend no significa crear un hilo nuevo. En este caso, Room proporciona el soporte para realizar las consultas de forma asíncrona.
3. Editar y eliminar por identificador permite modificar una tarea específica sin cambiar las demás. Para cerrar la validación del trabajo, todavía corresponde comprobar estas acciones y la persistencia en el emulador.
