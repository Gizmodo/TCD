package com.shop.tcd.di

import com.shop.tcd.ui.login.LoginViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        DataBaseModule::class
    ]
)
interface ViewModelInjector {
    fun inject(viewmodel: LoginViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun networkModule(networkModule: NetworkModule): Builder
        fun databaseModule(dataBaseModule: DataBaseModule): Builder
    }
}