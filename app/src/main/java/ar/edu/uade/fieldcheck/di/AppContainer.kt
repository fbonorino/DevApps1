package ar.edu.uade.fieldcheck.di

import android.content.Context
import ar.edu.uade.fieldcheck.data.connectivity.FakeNetworkMonitor
import ar.edu.uade.fieldcheck.data.local.database.AppDatabase
import ar.edu.uade.fieldcheck.data.repository.RoomInspectionRepository
import ar.edu.uade.fieldcheck.data.repository.RoomTemplateRepository
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import ar.edu.uade.fieldcheck.domain.repository.TemplateRepository
import ar.edu.uade.fieldcheck.domain.usecase.CreateInspectionUseCase
import ar.edu.uade.fieldcheck.domain.usecase.FinalizeInspectionUseCase

// Inyección manual: acá se crean las dependencias una sola vez y los ViewModels las reciben.
// Los repositorios falsos (FakeInspectionRepository, FakeTemplateRepository) siguen disponibles
// para los previews y los tests.
class AppContainer(context: Context) {

    val database: AppDatabase = AppDatabase.build(context)

    // Todavía no hay ConnectivityManager: para probar el banner offline, cambiar a false
    val networkMonitor: NetworkMonitor = FakeNetworkMonitor(online = true)

    val inspectionRepository: InspectionRepository = RoomInspectionRepository(database.inspectionDao())
    val templateRepository: TemplateRepository = RoomTemplateRepository(database.templateDao())

    // Casos de uso: solo donde hay una regla de negocio
    val createInspection = CreateInspectionUseCase(inspectionRepository)
    val finalizeInspection = FinalizeInspectionUseCase(inspectionRepository)
}
