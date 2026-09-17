package br.com.movva.core.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

@Composable
actual fun rememberPlatformContext(): Any? = LocalContext.current

actual fun KoinApplication.providePlatformContext(context: Any?) {
    androidContext(context as Context)
}
