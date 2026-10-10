package br.com.movva.feature.treino.di

import br.com.movva.feature.treino.data.TreinoLocalDataSource
import br.com.movva.feature.treino.data.TreinoLocalDataSourceImpl
import br.com.movva.feature.treino.data.TreinoRemoteDataSource
import br.com.movva.feature.treino.data.TreinoRemoteDataSourceImpl
import br.com.movva.feature.treino.data.TreinoRepository
import br.com.movva.feature.treino.data.TreinoRepositoryImpl
import org.koin.dsl.module

val treinoModule = module {
    single<TreinoLocalDataSource> { TreinoLocalDataSourceImpl(get()) }
    single<TreinoRemoteDataSource> { TreinoRemoteDataSourceImpl(get()) }
    single<TreinoRepository> { TreinoRepositoryImpl(get(), get(), get()) }
}