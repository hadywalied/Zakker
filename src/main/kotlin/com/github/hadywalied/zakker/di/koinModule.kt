package com.github.hadywalied.zakker.di

import com.github.hadywalied.zakker.application.AzkarService
import com.github.hadywalied.zakker.domain.IAzkarRepository
import com.github.hadywalied.zakker.infrastructure.DatabaseConfig
import com.github.hadywalied.zakker.infrastructure.SqlAzkarRepository
import org.koin.dsl.module

val pluginModule = module {
    single {
        DatabaseConfig()
    }
    single<IAzkarRepository>{
        SqlAzkarRepository(get())
    }
    single {
        AzkarService(get())
    }
}