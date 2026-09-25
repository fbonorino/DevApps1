package ar.edu.uade.fieldcheck.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.presentation.execution.ExecutionScreen
import ar.edu.uade.fieldcheck.presentation.execution.ExecutionViewModel
import ar.edu.uade.fieldcheck.presentation.history.HistoryScreen
import ar.edu.uade.fieldcheck.presentation.history.HistoryViewModel
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionEvent
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionScreen
import ar.edu.uade.fieldcheck.presentation.newinspection.NewInspectionViewModel
import ar.edu.uade.fieldcheck.presentation.review.ReviewEvent
import ar.edu.uade.fieldcheck.presentation.review.ReviewScreen
import ar.edu.uade.fieldcheck.presentation.review.ReviewViewModel
import kotlinx.coroutines.launch

// Flujo principal de docs/diagramas.md (diagrama 1). Cada destino crea su ViewModel,
// junta el estado y se lo pasa a una pantalla stateless.
@Composable
fun FieldCheckNavHost(container: AppContainer) {
    val navController = rememberNavController()
    // Snackbar del historial: lo usan otras pantallas para avisar algo al volver a P1
    val historySnackbarHostState = remember { SnackbarHostState() }
    val appScope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = HistoryRoute) {

        composable<HistoryRoute> {
            val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            HistoryScreen(
                uiState = uiState,
                isOffline = isOffline,
                onNewInspection = { navController.navigate(NewInspectionRoute) },
                onInspectionClick = { inspection ->
                    if (inspection.status == InspectionStatus.IN_PROGRESS) {
                        navController.navigate(ExecutionRoute(inspection.id))
                    }
                    // TODO P5: las finalizadas van al detalle
                },
                onRetry = viewModel::retry,
                snackbarHostState = historySnackbarHostState,
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
                        // P2 sale de la pila: "atrás" desde P3 vuelve a P1
                        is NewInspectionEvent.InspectionCreated -> navController.navigate(ExecutionRoute(event.inspectionId)) {
                            popUpTo(HistoryRoute)
                        }
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
    
        composable<ExecutionRoute> { entry ->
            val route = entry.toRoute<ExecutionRoute>()
            val viewModel: ExecutionViewModel = viewModel(
                factory = ExecutionViewModel.factory(container, route.inspectionId, route.itemId),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val photoMessage = stringResource(R.string.execution_photo_not_available)
            val haptic = LocalHapticFeedback.current
            val scope = rememberCoroutineScope()

            ExecutionScreen(
                uiState = uiState,
                isOffline = isOffline,
                onBack = { navController.popBackStack() },
                onSeeAll = { navController.navigate(ReviewRoute(route.inspectionId)) },
                onStatusSelected = { status ->
                    // Vibración corta como confirmación sin mirar la pantalla
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    viewModel.onStatusSelected(status)
                },
                onNoteChange = viewModel::onNoteChange,
                onAddPhoto = { scope.launch { snackbarHostState.showSnackbar(photoMessage) } },
                onPrevious = viewModel::onPrevious,
                onNext = viewModel::onNext,
                onRetry = viewModel::retry,
                snackbarHostState = snackbarHostState,
            )
        }

        composable<ReviewRoute> { entry ->
            val route = entry.toRoute<ReviewRoute>()
            val viewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.factory(container, route.inspectionId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            val finishedOffline = stringResource(R.string.review_finished_offline)
            val finishedOnline = stringResource(R.string.review_finished_online)

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ReviewEvent.Finished -> {
                            navController.popBackStack(HistoryRoute, inclusive = false)
                            // Se muestra en P1; el scope es del NavHost porque esta pantalla ya se cerró
                            appScope.launch {
                                historySnackbarHostState.showSnackbar(if (event.wasOffline) finishedOffline else finishedOnline)
                            }
                        }
                    }
                }
            }

            ReviewScreen(
                uiState = uiState,
                isOffline = isOffline,
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    // Vuelve a P3 en ese ítem. Se reemplaza la P3 anterior para que "atrás" siga yendo a P1.
                    navController.navigate(ExecutionRoute(route.inspectionId, item.id)) {
                        popUpTo<ExecutionRoute> { inclusive = true }
                    }
                },
                onFinishClick = viewModel::onFinishClick,
                onConfirmFinish = viewModel::onConfirmFinish,
                onDismissDialog = viewModel::onDismissDialog,
                onRetry = viewModel::retry,
                onGoHome = { navController.popBackStack(HistoryRoute, inclusive = false) },
            )
        }
    }
}
