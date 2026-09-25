package ar.edu.uade.fieldcheck.di

import ar.edu.uade.fieldcheck.data.connectivity.FakeNetworkMonitor
import ar.edu.uade.fieldcheck.data.repository.FakeInspectionRepository
import ar.edu.uade.fieldcheck.data.repository.FakeScenario
import ar.edu.uade.fieldcheck.data.repository.FakeTemplateRepository
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import ar.edu.uade.fieldcheck.domain.repository.TemplateRepository
import ar.edu.uade.fieldcheck.domain.usecase.CreateInspectionUseCase

// Inyección manual: acá se crean las dependencias una sola vez y los ViewModels las reciben
class AppContainer {

    // Para ver los distintos estados en el emulador: cambiar estos valores y volver a correr la app
    private val fakeScenario = FakeScenario.CONTENT
    private val fakeOnline = true

    val networkMonitor: NetworkMonitor = FakeNetworkMonitor(fakeOnline)
    val inspectionRepository: InspectionRepository = FakeInspectionRepository(fakeScenario)
    val templateRepository: TemplateRepository = FakeTemplateRepository(fakeScenario, fakeOnline)

    // Casos de uso: solo donde hay una regla de negocio
    val createInspection = CreateInspectionUseCase(inspectionRepository)
}
