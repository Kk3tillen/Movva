package br.com.movva.modules

import br.com.movva.auth.AuthGateViewModel
import br.com.movva.auth.LoginViewModel
import br.com.movva.auth.data.AuthRepositoryImpl
import br.com.movva.auth.domain.AuthRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    viewModel { AuthGateViewModel(get()) }
    viewModel { LoginViewModel(get()) }
}