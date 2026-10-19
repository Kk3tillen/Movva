package br.com.movva.feature.home.di

import br.com.movva.feature.home.ui.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
}
