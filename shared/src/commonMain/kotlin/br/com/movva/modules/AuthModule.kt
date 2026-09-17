package br.com.movva.modules

import br.com.movva.auth.AuthGateViewModel
import br.com.movva.auth.AuthRepository
import br.com.movva.auth.AuthRepositoryImpl
import br.com.movva.auth.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    viewModel { AuthGateViewModel(get()) }
    viewModel { LoginViewModel(get()) }
}