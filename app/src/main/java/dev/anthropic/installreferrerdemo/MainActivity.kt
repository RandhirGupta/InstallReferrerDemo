package dev.anthropic.installreferrerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.anthropic.installreferrerdemo.ui.screens.ReferrerScreen
import dev.anthropic.installreferrerdemo.ui.screens.ReferrerViewModel
import dev.anthropic.installreferrerdemo.ui.theme.InstallReferrerDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstallReferrerDemoTheme {
                val viewModel: ReferrerViewModel = viewModel()
                ReferrerScreen(viewModel = viewModel)
            }
        }
    }
}
