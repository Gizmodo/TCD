package com.shop.tcd.v2.screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.shop.tcd.R
import com.shop.tcd.databinding.ActivityMainBinding
import com.shop.tcd.ui.InventarisationActivity
import java.lang.Deprecated

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        /*  navHostFragment.findNavController()
              .navigate(LoginFragmentDirections.actionLoginFragmentToPrintFragment())*/
    }

    /**
     * Открывает экран "Пересчет товаров".
     * Действие по кнопке на главном экране.
     */
    @Suppress("UNUSED_PARAMETER")
    @Deprecated
    fun btnShowRecalc(view: View) {
        val intent = Intent(this, InventarisationActivity::class.java)
        startActivity(intent)
    }
}