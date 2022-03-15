package com.shop.tcd.v2.screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.shop.tcd.R
import com.shop.tcd.databinding.ActivityMainBinding
import com.shop.tcd.ui.CatalogueActivity
import com.shop.tcd.ui.EXTRA_MESSAGE
import com.shop.tcd.ui.InventarisationActivity
import com.shop.tcd.ui.NomenclatureActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    /**
     * Открывает экран "Загрузить каталог товаров".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowCatalogue(view: View) {
        val intent = Intent(this, CatalogueActivity::class.java)
            .apply {
                putExtra(EXTRA_MESSAGE, "catalog")
            }
        startActivity(intent)
    }

    /**
     * Открывает экран "Справочник Номенклатура".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowNomenclature(view: View) {
        val intent = Intent(this, NomenclatureActivity::class.java)
            .apply {
                putExtra(EXTRA_MESSAGE, "nomenclature")
            }
        startActivity(intent)
    }

    /**
     * Открывает экран "Пересчет товаров".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowRecalc(view: View) {
        val intent = Intent(this, InventarisationActivity::class.java)
            .apply {
                putExtra(EXTRA_MESSAGE, "recalc")
            }
        startActivity(intent)
    }
}