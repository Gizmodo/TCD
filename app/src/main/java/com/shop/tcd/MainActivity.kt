package com.shop.tcd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

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

    /** Called when the user taps the Send button */
    fun showLoadGoodsView(view: View) {
//        val editText = findViewById<TextInputEditText>(R.id.edLogin)
//        val message = editText.text.toString()
        val intent = Intent(this, NomenklaturaActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "test")
        }
        startActivity(intent)
    }
}