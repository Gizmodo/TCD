package com.shop.tcd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shop.tcd.databinding.ActivityDetailBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.bundlizer.Bundlizer
import kotlinx.serialization.KSerializer

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val deserializer: KSerializer<InvItem> = InvItem.serializer()
        intent.getBundleExtra("item")?.let { bundle ->
            Bundlizer.unbundle(deserializer, bundle).let { invItem ->
                with(binding) {
                    txtDetailBarcode.text = invItem.barcode
                    txtDetailCode.text = invItem.code
                    txtDetailCount.text = invItem.quantity
                    txtDetailGood.text = invItem.name
                    txtDetailPLU.text = invItem.plu
                }

            }
        }
    }
}