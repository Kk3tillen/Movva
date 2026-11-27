package br.com.movva.components.perfil.di

import app.cash.sqldelight.db.SqlDriver
import br.com.movva.core.db.DatabaseDriverFactory
import br.com.movva.components.perfil.data.PerfilLocalDataSource
import br.com.movva.components.perfil.data.PerfilLocalDataSourceImpl
import br.com.movva.components.perfil.data.PerfilRemoteDataSource
import br.com.movva.components.perfil.data.PerfilRemoteDataSourceImpl
import br.com.movva.components.perfil.data.PerfilRepositoryImpl
import br.com.movva.components.perfil.domain.PerfilRepository
import br.com.movva.components.perfil.ui.PerfilViewModel
import br.com.movva.shared.db.MovvaDatabase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val perfilModule = module {
    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }
    single { MovvaDatabase(get()) }
    single<PerfilLocalDataSource> { PerfilLocalDataSourceImpl(get()) }
    single<PerfilRemoteDataSource> { PerfilRemoteDataSourceImpl(get()) }
    single<PerfilRepository> { PerfilRepositoryImpl(get(), get(), get()) }
    viewModel { PerfilViewModel(get()) }
}
