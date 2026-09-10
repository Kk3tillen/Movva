package br.com.movva.modules

import br.com.movva.auth.AuthRepository
import br.com.movva.auth.AuthRepositoryImpl
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthGateViewModel(get()) }
}