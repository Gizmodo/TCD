package com.shop.tcd.ui

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.shop.tcd.databinding.ActivityPrintBinding
import com.shop.tcd.model.newsettigs.PrintersList
import com.shop.tcd.repository.settings.RepositorySettings
import com.shop.tcd.repository.settings.RetrofitServiceSettings
import com.shop.tcd.utils.Common
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class PrintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrintBinding
    private val retrofitService = RetrofitServiceSettings.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadPrintersList()
    }

    private fun loadPrintersList() {
        val repository = RepositorySettings(retrofitService)
        val response = repository.getPrintersList()

        response.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d(it.printers.toString())
                setupAutoComplete(binding.edtPrinter,
                it)
            }, {
                Timber.e("Load printers list failed - $it")
            })
    }

    private fun setupAutoComplete(view: AutoCompleteTextView, items: PrintersList) {
        val printersIP: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return items.printers[index].ip
            }

            override val size: Int
                get() = items.printers.size
        }
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, printersIP)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            Common.selectedPrinter = items.printers[position]
            Common.selectedPrinterPosition = position
        }
        if (Common.selectedPrinterPosition != -1) {
            view.setText(adapter.getItem(Common.selectedPrinterPosition), false)
        }
    }

    private fun btnPrint(view: View) {}
}