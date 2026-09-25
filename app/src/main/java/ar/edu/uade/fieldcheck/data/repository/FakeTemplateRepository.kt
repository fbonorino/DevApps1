package ar.edu.uade.fieldcheck.data.repository

import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.repository.TemplateRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException

class FakeTemplateRepository(
    private val scenario: FakeScenario,
    private val isOnline: Boolean,
    private val downloadDelayMs: Long = 1500,
) : TemplateRepository {

    private val threeDaysAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L

    // Sin conexión simulamos una caché de hace 3 días, para mostrar el aviso de antigüedad
    private val cache = MutableStateFlow(
        if (scenario == FakeScenario.CONTENT) FakeData.templates(downloadedAt = threeDaysAgo) else emptyList()
    )

    override fun observeTemplates(): Flow<List<Template>> = flow {
        delay(200)
        emitAll(cache)
    }

    override suspend fun refreshTemplates() {
        delay(downloadDelayMs)
        if (!isOnline) throw IOException("Sin conexión")
        when (scenario) {
            FakeScenario.ERROR -> throw IOException("El servidor no respondió")
            FakeScenario.EMPTY -> cache.value = emptyList()
            FakeScenario.CONTENT -> {
                val now = System.currentTimeMillis()
                cache.update { list -> list.map { it.copy(downloadedAt = now) } }
            }
        }
    }
}
