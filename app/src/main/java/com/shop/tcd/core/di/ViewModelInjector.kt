package com.shop.tcd.core.di

import com.shop.tcd.screen.catalog.CatalogViewModel
import com.shop.tcd.screen.catalog.group.GroupsViewModel
import com.shop.tcd.screen.inventory.InventoryViewModel
import com.shop.tcd.screen.inventory.chronology.InventoryChronologyViewModel
import com.shop.tcd.screen.inventory.detail.InventoryItemDetailViewModel
import com.shop.tcd.screen.login.LoginViewModel
import com.shop.tcd.screen.main.MainViewModel
import com.shop.tcd.screen.nomenclature.NomenclatureViewModel
import com.shop.tcd.screen.print.PrintViewModel
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
    fun inject(viewmodel: InventoryViewModel)
    fun inject(viewmodel: InventoryItemDetailViewModel)
    fun inject(viewmodel: InventoryChronologyViewModel)

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
