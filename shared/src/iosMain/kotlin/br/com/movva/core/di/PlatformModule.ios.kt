package br.com.movva.core.di

import br.com.movva.core.clipboard.ClipboardManager
import br.com.movva.core.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory() }
    single { ClipboardManager() }
}
