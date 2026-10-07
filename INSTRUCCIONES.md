# Ejecutar y presentar el laboratorio 08

## Abrir el proyecto

1. Extrae el ZIP en una carpeta local, por ejemplo C:\Android\Lab08.
2. En Android Studio selecciona Open y abre la carpeta Lab08 que contiene settings.gradle.kts. No abras solamente app.
3. En Settings > Build, Execution, Deployment > Build Tools > Gradle, selecciona Gradle JDK 17. Usa la distribución del wrapper incluido.
4. Instala Android SDK Platform 34 si Android Studio lo solicita y sincroniza el proyecto. La primera sincronización requiere conexión a Internet.
5. Selecciona un emulador o teléfono con Android 7.0/API 24 o superior y pulsa Run.

Se mantienen versiones concretas para reproducir el laboratorio. No es necesario aceptar una actualización de AGP. Este proyecto es una app de tareas independiente, como pide el documento.

## Archivos, en el orden de la guía

1. app/build.gradle.kts: Kapt y dependencias.
2. app/src/main/java/com/example/lab08/Task.kt: modelo.
3. TaskDao.kt, en el mismo paquete: operaciones de la base de datos.
4. TaskDatabase.kt: base de datos.
5. TaskViewModel.kt: estado y eventos.
6. MainActivity.kt: interfaz; ui/theme/Theme.kt: tema.
7. RESPUESTAS.md: investigaciones, explicación del ejercicio, observaciones y conclusiones.

## Evidencia de la parte guiada

El historial conserva una versión anterior al ejercicio. Para abrirla sin alterar main, detén la app, abre la terminal del proyecto y ejecuta:

```bash
git switch --detach parte-guiada
```

Ejecuta la app y registra evidencia real de este recorrido:

1. Agrega “Estudiar Kotlin” y “Terminar laboratorio”. Deben aparecer ambas.
2. Marca la primera como completada y deja la segunda pendiente.
3. Cierra y vuelve a abrir la app. Ambas deben conservarse con su estado.
4. Usa “Eliminar todas las tareas”. La lista debe quedar vacía.

La guía solicita mostrar el funcionamiento; puedes usar capturas del recorrido o una grabación según lo que admita tu entrega. No se incluyen capturas simuladas.

## Evidencia del ejercicio 1

Regresa a la versión final:

```bash
git switch main
```

Sincroniza si Android Studio lo solicita y vuelve a ejecutar.

1. Agrega dos tareas. Captura la vista con cabecera azul y tarjetas.
2. Edita una descripción y guarda. Captura el resultado.
3. Elimina una tarea individual. Comprueba que la otra permanezca.
4. Cierra y abre de nuevo para verificar que la edición y la eliminación se guardaron.

El documento pide elegir dos características: aquí son editar y eliminar individualmente.

## Compartir el repositorio

Se incluye un repositorio local con commits por avance, registrados por Codex. Para conservarlo, extrae también la carpeta .git. No vuelvas a ejecutar git init ni reemplaces el historial.

Crea un repositorio vacío llamado Lab08 en tu cuenta de GitHub, sin README ni .gitignore inicial. Desde la terminal en Lab08, reemplaza TU_USUARIO por tu usuario real:

```bash
git switch main
git remote add origin https://github.com/TU_USUARIO/Lab08.git
git push -u origin main
git push origin --tags
```

Comparte la URL real en la entrega. Los pushes requieren tu sesión o credenciales de GitHub. El repositorio no se ha publicado desde este entorno.

Consulta los avances con:

```bash
git log --oneline --all --decorate
```

## Estado de verificación

Los archivos se revisaron estructuralmente. Se intentó ejecutar `./gradlew :app:assembleDebug --no-daemon`, pero la descarga de Gradle falló con `UnknownHostException: services.gradle.org`. Este entorno tampoco dispone de un SDK Android configurado ni emulador. No se afirma que la app esté compilada, ejecutada o que las pruebas de persistencia hayan pasado. La validación pendiente se realiza con los recorridos indicados arriba.

Los módulos de bonificación de Room no forman parte de esta solución.
