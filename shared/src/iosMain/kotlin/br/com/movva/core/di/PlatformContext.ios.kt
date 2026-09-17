package br.com.movva.core.di

import androidx.compose.runtime.Composable
import org.koin.core.KoinApplication

@Composable
actual fun rememberPlatformContext(): Any? = null

actual fun KoinApplication.providePlatformContext(context: Any?) {
    // iOS não precisa de um Context de plataforma para o Koin.
}
