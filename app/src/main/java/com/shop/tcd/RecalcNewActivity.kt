package com.shop.tcd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.broadcast.TCDBroadcastListener
import com.shop.tcd.broadcast.TCDBroadcastReceiver
import com.shop.tcd.bundlizer.bundle
import com.shop.tcd.databinding.ActivityZebraBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.viewmodel.RecalcViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class RecalcNewActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityZebraBinding
    private val model: RecalcViewModel by viewModels()
    private var adapter: InvAdapter? = null
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private var list = mutableListOf<InvItem>()

    private var iDataBarcode: TCDBroadcastReceiver? = null
    private var urovoBarcode: TCDBroadcastReceiver? = null
    private var urovoKeyboard: TCDBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZebraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        attachHideKeyboardListeners()
        initBarcodeListener()
    }

    private fun attachHideKeyboardListeners() {
        binding.textInputLayoutCount.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.textInputLayoutBarcode.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtCount.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtBarcode.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
    }

    private fun initBarcodeListener() {
        iDataBarcode = TCDBroadcastReceiver()
        iDataBarcode.also {
            it?.apply {
                listener = object : TCDBroadcastListener {
                    override fun onSuccess(message: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onError(message: String) {
                        TODO("Not yet implemented")
                    }

                }
            }
        }

        urovoBarcode = TCDBroadcastReceiver()
        urovoBarcode.also {
            it?.apply {
                listener = object : TCDBroadcastListener {
                    override fun onSuccess(message: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onError(message: String) {
                        TODO("Not yet implemented")
                    }

                }
            }
        }
        urovoKeyboard = TCDBroadcastReceiver()
        urovoKeyboard.also {
            it?.apply {
                listener = object : TCDBroadcastListener {
                    override fun onSuccess(message: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onError(message: String) {
                        TODO("Not yet implemented")
                    }

                }
            }
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)

        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerView
        adapter = InvAdapter(list, onItemClick)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecalcNewActivity)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@RecalcNewActivity, LinearLayout.VERTICAL))
            adapter = adapter
        }

    }

    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem) {
            Timber.d("Item clicked with " + invItem.name)
            val bundle: Bundle = invItem.bundle(InvItem.serializer())
            val intent = Intent(this@RecalcNewActivity, DetailActivity::class.java)
                .apply {
                    putExtra("item", bundle)
                }
            startActivity(intent, bundle)
        }
    }
}