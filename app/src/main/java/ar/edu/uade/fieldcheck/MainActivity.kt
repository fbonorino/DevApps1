package ar.edu.uade.fieldcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ar.edu.uade.fieldcheck.presentation.navigation.FieldCheckNavHost
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FieldCheckApplication).container
        setContent {
            FieldCheckTheme {
                FieldCheckNavHost(container)
            }
        }
    }
}
