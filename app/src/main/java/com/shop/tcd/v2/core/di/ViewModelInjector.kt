package com.shop.tcd.v2.core.di

import com.shop.tcd.v2.screen.catalog.CatalogViewModel
import com.shop.tcd.v2.screen.catalog.group.GroupsViewModel
import com.shop.tcd.v2.screen.login.LoginViewModel
import com.shop.tcd.v2.screen.main.MainViewModel
import com.shop.tcd.v2.screen.nomenclature.NomenclatureViewModel
import com.shop.tcd.v2.screen.print.PrintViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        DataBaseModule::class,
        DataSourceModule::class,
        DataStoreModule::class
    ]
)
interface ViewModelInjector {
    fun inject(viewmodel: LoginViewModel)
    fun inject(viewmodel: MainViewModel)
    fun inject(viewmodel: PrintViewModel)
    fun inject(viewmodel: CatalogViewModel)
    fun inject(viewmodel: GroupsViewModel)
    fun inject(viewmodel: NomenclatureViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun app(appModule: AppModule): Builder
        fun nm(networkModule: NetworkModule): Builder
        fun dbm(dataBaseModule: DataBaseModule): Builder
        fun dbh(databaseHelper: DataSourceModule): Builder
        fun datastore(datastore: DataStoreModule): Builder
    }
}
