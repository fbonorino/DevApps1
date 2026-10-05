package ar.edu.uade.fieldcheck

import android.app.Application
import ar.edu.uade.fieldcheck.data.local.database.seedTemplatesIfEmpty
import ar.edu.uade.fieldcheck.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FieldCheckApplication : Application() {
    lateinit var container: AppContainer
        private set

    // Scope que vive mientras vive la app, para trabajo que no depende de una pantalla
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Las pantallas observan la base, así que se actualizan solas cuando termina
        applicationScope.launch { seedTemplatesIfEmpty(container.database.templateDao()) }
    }
}
