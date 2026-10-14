package br.com.movva.feature.dieta.di

import br.com.movva.feature.dieta.data.DietaLocalDataSource
import br.com.movva.feature.dieta.data.DietaLocalDataSourceImpl
import br.com.movva.feature.dieta.data.DietaRemoteDataSource
import br.com.movva.feature.dieta.data.DietaRemoteDataSourceImpl
import br.com.movva.feature.dieta.data.DietaRepository
import br.com.movva.feature.dieta.data.DietaRepositoryImpl
import br.com.movva.feature.dieta.ui.DietaViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dietaModule = module {
    single<DietaLocalDataSource> { DietaLocalDataSourceImpl(get()) }
    single<DietaRemoteDataSource> { DietaRemoteDataSourceImpl(get()) }
    single<DietaRepository> { DietaRepositoryImpl(get(), get(), get()) }
    viewModel { DietaViewModel(get()) }
}