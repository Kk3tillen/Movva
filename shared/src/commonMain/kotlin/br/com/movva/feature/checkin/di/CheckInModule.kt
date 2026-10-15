package br.com.movva.feature.checkin.di

import br.com.movva.feature.checkin.data.CheckInLocalDataSource
import br.com.movva.feature.checkin.data.CheckInLocalDataSourceImpl
import br.com.movva.feature.checkin.data.CheckInRemoteDataSource
import br.com.movva.feature.checkin.data.CheckInRemoteDataSourceImpl
import br.com.movva.feature.checkin.data.CheckInRepository
import br.com.movva.feature.checkin.data.CheckInRepositoryImpl
import br.com.movva.feature.checkin.ui.CheckInViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val checkInModule = module {
    single<CheckInLocalDataSource> { CheckInLocalDataSourceImpl(get()) }
    single<CheckInRemoteDataSource> { CheckInRemoteDataSourceImpl(get()) }
    single<CheckInRepository> { CheckInRepositoryImpl(get(), get(), get()) }
    viewModel { CheckInViewModel(get()) }
}
