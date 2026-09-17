package br.com.movva.feature.perfil.di

import br.com.movva.core.db.DatabaseDriverFactory
import br.com.movva.feature.perfil.data.PerfilLocalDataSource
import br.com.movva.feature.perfil.data.PerfilLocalDataSourceImpl
import br.com.movva.feature.perfil.data.PerfilRemoteDataSource
import br.com.movva.feature.perfil.data.PerfilRemoteDataSourceImpl
import br.com.movva.feature.perfil.data.PerfilRepository
import br.com.movva.feature.perfil.data.PerfilRepositoryImpl
import br.com.movva.feature.perfil.ui.PerfilViewModel
import br.com.movva.shared.db.MovvaDatabase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val perfilModule = module {
    single { MovvaDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single<PerfilLocalDataSource> { PerfilLocalDataSourceImpl(get()) }
    single<PerfilRemoteDataSource> { PerfilRemoteDataSourceImpl(get()) }
    single<PerfilRepository> { PerfilRepositoryImpl(get(), get(), get()) }
    viewModel { PerfilViewModel(get()) }
}
