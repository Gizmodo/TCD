package com.shop.tcd.di

import com.shop.tcd.ui.login.LoginViewModel
import com.shop.tcd.ui.login.LoginViewModel1
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
//        DataBaseModule::class,
        DataSourceModule::class
    ]
)
interface ViewModelInjector {
    fun inject(vm: LoginViewModel)
    fun inject(vm: LoginViewModel1)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun networkModule(networkModule: NetworkModule): Builder
//        fun databaseModule(dataBaseModule: DataBaseModule): Builder
        fun datasourceModule(dsm: DataSourceModule): Builder
    }
}