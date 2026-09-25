package ar.edu.uade.fieldcheck.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.presentation.history.HistoryScreen
import ar.edu.uade.fieldcheck.presentation.history.HistoryViewModel

// Flujo principal de docs/diagramas.md (diagrama 1). Cada destino crea su ViewModel,
// junta el estado y se lo pasa a una pantalla stateless.
@Composable
fun FieldCheckNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HistoryRoute) {

        composable<HistoryRoute> {
            val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            HistoryScreen(
                uiState = uiState,
                isOffline = isOffline,
                onNewInspection = { /* P2: próximo commit */ },
                onInspectionClick = { /* P3 y P5: próximos commits */ },
                onRetry = viewModel::retry,
            )
        }
    }
}
