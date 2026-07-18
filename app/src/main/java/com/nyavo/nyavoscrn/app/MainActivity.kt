package com.nyavo.nyavoscrn.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nyavo.nyavoscrn.core.designsystem.theme.NyavoSCRNTheme
import com.nyavo.nyavoscrn.features.screentest.ui.ScreenTestScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NyavoSCRNTheme {
                ScreenTestScreen()
            }
        }
    }
}
