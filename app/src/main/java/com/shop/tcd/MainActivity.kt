package com.shop.tcd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.textView).apply {
            text = message
        }
    }

    /**
     * Открывает экран "Загрузить каталог товаров".
     * Действие по кнопке на главном экране.
     */
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
    fun btnShowRecalc(view: View) {
        val intent = Intent(this, RecalcActivity::class.java)
            .apply {
                putExtra(EXTRA_MESSAGE, "recalc")
            }
        startActivity(intent)
    }

    /**
     * Открывает экран "Печать ценников".
     * Действие по кнопке на главном экране.
     */
    fun btnShowPrintLabels(view: View) {

    }
}