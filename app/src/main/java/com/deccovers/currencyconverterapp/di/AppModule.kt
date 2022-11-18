package com.deccovers.currencyconverterapp.di

import com.deccovers.currencyconverterapp.data.ExchangeRepository
import com.deccovers.currencyconverterapp.data.ExchangeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideExchangeRepository(): ExchangeRepository = ExchangeRepositoryImpl()
}