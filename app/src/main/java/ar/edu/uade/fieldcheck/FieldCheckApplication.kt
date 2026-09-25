package ar.edu.uade.fieldcheck

import android.app.Application
import ar.edu.uade.fieldcheck.di.AppContainer

class FieldCheckApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
