package com.shop.tcd.v2.di

import com.shop.tcd.v2.screen.login.LoginViewModel
import com.shop.tcd.v2.screen.main.MainViewModel
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
    fun inject(viewmodel: MainViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun networkModule(networkModule: NetworkModule): Builder
        fun databaseModule(dataBaseModule: DataBaseModule): Builder
    }
}