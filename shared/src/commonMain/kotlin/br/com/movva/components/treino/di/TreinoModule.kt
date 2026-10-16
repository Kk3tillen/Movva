package br.com.movva.components.treino.di

import br.com.movva.components.treino.data.TreinoLocalDataSource
import br.com.movva.components.treino.data.TreinoLocalDataSourceImpl
import br.com.movva.components.treino.data.TreinoRemoteDataSource
import br.com.movva.components.treino.data.TreinoRemoteDataSourceImpl
import br.com.movva.components.treino.data.TreinoRepository
import br.com.movva.components.treino.data.TreinoRepositoryImpl
import org.koin.dsl.module
import br.com.movva.components.treino.ui.TreinoDetalheViewModel
import br.com.movva.components.treino.ui.TreinoViewModel
import org.koin.core.module.dsl.viewModel

val treinoModule = module {
    single<TreinoLocalDataSource> { TreinoLocalDataSourceImpl(get()) }
    single<TreinoRemoteDataSource> { TreinoRemoteDataSourceImpl(get()) }
    single<TreinoRepository> { TreinoRepositoryImpl(get(), get(), get(), get()) }
    viewModel { TreinoViewModel(get()) }
    viewModel { params -> TreinoDetalheViewModel(treinoId = params.get(), treinoRepository = get()) }
}