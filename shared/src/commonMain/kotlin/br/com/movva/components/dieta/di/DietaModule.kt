package br.com.movva.components.dieta.di

import br.com.movva.components.dieta.data.DietaLocalDataSource
import br.com.movva.components.dieta.data.DietaLocalDataSourceImpl
import br.com.movva.components.dieta.data.DietaRemoteDataSource
import br.com.movva.components.dieta.data.DietaRemoteDataSourceImpl
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietaRepositoryImpl
import br.com.movva.components.dieta.ui.DietaViewModel
import br.com.movva.components.dieta.ui.RegistrarRefeicaoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dietaModule = module {
    single<DietaLocalDataSource> { DietaLocalDataSourceImpl(get()) }
    single<DietaRemoteDataSource> { DietaRemoteDataSourceImpl(get()) }
    single<DietaRepository> { DietaRepositoryImpl(get(), get(), get(), get()) }
    viewModel { DietaViewModel(get(), get()) }
    viewModel { params -> RegistrarRefeicaoViewModel(args = params.get(), dietaRepository = get()) }
}