package ar.edu.uade.fieldcheck.domain.repository

import kotlinx.coroutines.flow.Flow

// Solo alimenta el banner "sin conexión" de la UI
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
