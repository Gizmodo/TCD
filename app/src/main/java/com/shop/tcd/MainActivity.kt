package com.shop.tcd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.ActivityMainBinding
import com.shop.tcd.model.settings.Shop
import com.shop.tcd.utils.Common

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun isShopSelected(): Boolean {
        return Common.isInit()
    }

    private fun showError() {
        FancyToast.makeText(applicationContext,
            "Не выбран магазин",
            FancyToast.LENGTH_SHORT,
            FancyToast.ERROR,
            false).show()
    }

    private fun setupAutoComplete(view: AutoCompleteTextView, objects: List<Shop>) {
        val names: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return objects[index].shopName
            }

            override val size: Int
                get() = objects.size
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
//            val selected = parent?.adapter?.getItem(position) as String
//            val address = objects[position].shopURL
            Common.selectedShop = objects[position]
            Common.selectedUserPosition = position

            fun getIP(raw: String): String? {
                val matchResult: MatchResult? =
                    Regex("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b").find(raw)
                return matchResult?.groupValues?.first()
            }


            Common.BASE_URL = objects[position].shopURL
        }
        if (Common.selectedUserPosition != -1) {
            view.setText(adapter.getItem(Common.selectedUserPosition), false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Common.shopsArray?.let { setupAutoComplete(binding.list, it) }

        /*  // Get the Intent that started this activity and extract the string
          val message = intent.getStringExtra(EXTRA_MESSAGE)

          // Capture the layout's TextView and set the string as its text
          val textView = findViewById<TextView>(R.id.textView).apply {
              text = message
          }*/
    }

    /**
     * Открывает экран "Загрузить каталог товаров".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowCatalogue(view: View) {
        if (isShopSelected()) {
            val intent = Intent(this, CatalogueActivity::class.java)
                .apply {
                    putExtra(EXTRA_MESSAGE, "catalog")
                }
            startActivity(intent)
        } else {
            showError()
        }
    }

    /**
     * Открывает экран "Справочник Номенклатура".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowNomenclature(view: View) {
        if (isShopSelected()) {
            val intent = Intent(this, NomenclatureActivity::class.java)
                .apply {
                    putExtra(EXTRA_MESSAGE, "nomenclature")
                }
            startActivity(intent)
        } else {
            showError()
        }
    }

    /**
     * Открывает экран "Пересчет товаров".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowRecalc(view: View) {
        if (isShopSelected()) {
            val intent = Intent(this, RecalcActivity::class.java)
                .apply {
                    putExtra(EXTRA_MESSAGE, "recalc")
                }
            startActivity(intent)
        } else {
            showError()
        }
    }

    /**
     * Открывает экран "Печать ценников".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    fun btnShowPrintLabels(view: View) {
        if (isShopSelected()) {
        } else {
            showError()
        }
    }
}