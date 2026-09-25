package ar.edu.uade.fieldcheck.data.repository

// Qué datos devuelven los repositorios falsos, para ver cada estado de las pantallas sin backend
enum class FakeScenario {
    CONTENT, // hay inspecciones y plantillas guardadas
    EMPTY,   // no hay nada guardado
    ERROR,   // falla la lectura local y la descarga de plantillas
}
