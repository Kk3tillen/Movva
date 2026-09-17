package br.com.movva.core.di

import androidx.compose.runtime.Composable
import org.koin.core.KoinApplication

@Composable
expect fun rememberPlatformContext(): Any?

expect fun KoinApplication.providePlatformContext(context: Any?)
