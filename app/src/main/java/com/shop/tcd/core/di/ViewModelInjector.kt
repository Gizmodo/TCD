package com.shop.tcd.core.di

import com.shop.tcd.ui.catalog.CatalogViewModel
import com.shop.tcd.ui.catalog.group.GroupsViewModel
import com.shop.tcd.ui.inventory.InventoryViewModel
import com.shop.tcd.ui.inventory.chronology.InventoryChronologyViewModel
import com.shop.tcd.ui.inventory.detail.InventoryItemDetailViewModel
import com.shop.tcd.ui.login.LoginViewModel
import com.shop.tcd.ui.main.MainViewModel
import com.shop.tcd.ui.nomenclature.NomenclatureViewModel
import com.shop.tcd.ui.overestimation.OverEstimationViewModel
import com.shop.tcd.ui.print.PrintViewModel
import com.shop.tcd.ui.remains.RemainsViewModel
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
    fun inject(viewmodel: OverEstimationViewModel)
    fun inject(viewmodel: RemainsViewModel)

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
