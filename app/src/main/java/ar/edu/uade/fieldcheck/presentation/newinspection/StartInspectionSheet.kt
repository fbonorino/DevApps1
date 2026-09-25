package ar.edu.uade.fieldcheck.presentation.newinspection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

// Hoja inferior para ingresar el sitio: deja el campo y el botón en la zona del pulgar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartInspectionSheet(
    templateName: String,
    onStart: (siteName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        StartInspectionSheetContent(templateName, onStart, autoFocus = true)
    }
}

// El texto del campo es estado de UI (no de negocio), por eso vive acá y no en el ViewModel
@Composable
fun StartInspectionSheetContent(
    templateName: String,
    onStart: (siteName: String) -> Unit,
    autoFocus: Boolean = false,
) {
    var siteName by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Foco y teclado abiertos al aparecer la hoja
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(templateName, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = siteName,
            onValueChange = {
                siteName = it
                if (it.isNotBlank()) showError = false
            },
            label = { Text(stringResource(R.string.site_label)) },
            placeholder = { Text(stringResource(R.string.site_placeholder)) },
            isError = showError,
            supportingText = if (showError) {
                { Text(stringResource(R.string.site_error)) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    // Muestra "Ingresá el sitio" recién cuando el campo pierde el foco vacío
                    if (state.isFocused) hadFocus = true
                    else if (hadFocus && siteName.isBlank()) showError = true
                },
        )
        Button(
            onClick = { onStart(siteName) },
            enabled = siteName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(stringResource(R.string.start_inspection))
        }
    }
}

@Preview
@Composable
private fun StartInspectionSheetPreview() {
    FieldCheckTheme {
        Surface { StartInspectionSheetContent("Control semanal de obra", onStart = {}) }
    }
}
