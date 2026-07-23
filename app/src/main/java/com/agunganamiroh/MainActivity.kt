package com.agunganamiroh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agunganamiroh.navigation.NavGraph
import com.agunganamiroh.ui.theme.AnamirohAgungTheme
import com.agunganamiroh.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            AnamirohAgungTheme(themeMode = themeMode) {
                NavGraph()
            }
        }
    }
}