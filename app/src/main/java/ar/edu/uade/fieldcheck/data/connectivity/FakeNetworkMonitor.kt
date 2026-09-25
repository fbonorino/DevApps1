package ar.edu.uade.fieldcheck.data.connectivity

import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Conexión fija para probar el banner offline. Después se reemplaza por uno con ConnectivityManager.
class FakeNetworkMonitor(online: Boolean) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(online)
}
