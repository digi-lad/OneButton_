package com.onebutton

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.onebutton.ui.screens.LoginScreen
import com.onebutton.ui.theme.OneButtonTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    OneButtonTheme {
        org.koin.compose.KoinApplication(application = {
            modules(com.onebutton.di.koinModules)
        }) {
            Navigator(LoginScreen())
        }
    }
}
