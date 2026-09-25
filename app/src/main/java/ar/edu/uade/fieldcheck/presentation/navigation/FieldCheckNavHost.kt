package ar.edu.uade.fieldcheck.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.presentation.history.HistoryScreen
import ar.edu.uade.fieldcheck.presentation.history.HistoryViewModel
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionEvent
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionScreen
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionViewModel

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
                onNewInspection = { navController.navigate(NewInspectionRoute) },
                onInspectionClick = { /* P3 y P5: próximos commits */ },
                onRetry = viewModel::retry,
            )
        }

        composable<NewInspectionRoute> {
            val viewModel: NewInspectionViewModel = viewModel(factory = NewInspectionViewModel.factory(container))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val refreshFailedMessage = stringResource(R.string.templates_refresh_failed)

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        // TODO P3: ir a la ejecución. Por ahora vuelve al historial, donde aparece "En curso".
                        is NewInspectionEvent.InspectionCreated -> navController.popBackStack()
                        NewInspectionEvent.RefreshFailed -> snackbarHostState.showSnackbar(refreshFailedMessage)
                    }
                }
            }

            NewInspectionScreen(
                uiState = uiState,
                isOffline = isOffline,
                onBack = { navController.popBackStack() },
                onRefresh = viewModel::refresh,
                onStartInspection = viewModel::startInspection,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}
