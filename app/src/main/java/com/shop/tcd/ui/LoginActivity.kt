package com.shop.tcd.ui

import android.R
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.ActivityLoginBinding
import com.shop.tcd.model.settings.GroupUser
import com.shop.tcd.model.settings.Shop
import com.shop.tcd.repository.settings.RepositorySettings
import com.shop.tcd.repository.settings.RetrofitServiceSettings
import com.shop.tcd.v2.screen.MainActivity
import com.shop.tcd.v2.screen.login.LoginViewModel
import com.shop.tcd.utils.Common
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.io.StringReader


const val EXTRA_MESSAGE = "com.shop.tcd.MESSAGE"

class LoginActivity : AppCompatActivity() {
    private val retrofitService = RetrofitServiceSettings.getInstance()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        showIPAddress()
        showVPNUsage()
        setContentView(binding.root)
        Common.usersArray.let { setupAutoComplete(binding.edtLogin, it) }
        loadSettings()
    }

    private fun showVPNUsage() {
        when {
            vpnActive(applicationContext) -> {
                binding.txtVPNStatus.text = "VPN используется"
            }
            else -> {
                binding.txtVPNStatus.text = "VPN не используется"
            }
        }
    }

    fun vpnActive(context: Context): Boolean {
        var vpnInUse = false
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork: Network? = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            return caps!!.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }
        val networks: Array<Network> = connectivityManager.allNetworks
        for (i in networks.indices) {
            val caps = connectivityManager.getNetworkCapabilities(networks[i])
            if (caps!!.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                vpnInUse = true
                break
            }
        }
        return vpnInUse
    }

    private fun showIPAddress() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        binding.txtIPAddress.text = ipAddress
    }

    private fun setupAutoComplete(view: AutoCompleteTextView, items: List<GroupUser>) {
        val names: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return items[index].userLogin
            }

            override val size: Int
                get() = items.size
        }
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, names)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            Common.selectedUser = items[position]
            Common.selectedUserPosition = position
        }
        if (Common.selectedUserPosition != -1) {
            view.setText(adapter.getItem(Common.selectedUserPosition), false)
        }
    }

    fun btnLogin(@Suppress("UNUSED_PARAMETER") view: View) {
        val userName = binding.edtLogin.text.toString()
        val userPassword = binding.edtPassword.text.toString()
        var isSuccess = false
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "empty")
        }

        Common.usersArray.forEach(fun(it: GroupUser) {
            val isFound = check(it, userName, userPassword)
            isSuccess = isSuccess || isFound
        })

        when {
            isSuccess -> {
                startActivity(intent)
            }
            else -> {
                FancyToast.makeText(
                    applicationContext,
                    "Неверно указаны данные для входа",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        }
    }

    private fun check(user: GroupUser, userName: String, userPassword: String): Boolean {
        val result = user.userLogin == userName && user.userPassword == userPassword
        if (result) {
            Common.selectedUser = user
        }
        return result
    }

    private fun parse1C(xmlResponseString: String) {
        val localShopList = ArrayList<HashMap<String, String?>>()
        val localUserList = ArrayList<HashMap<String, String?>>()
        try {
            var localUser = HashMap<String, String?>()
            var localShop = HashMap<String, String?>()

            val istreamStringWOEncoding = StringReader(xmlResponseString)

            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(istreamStringWOEncoding)

            var tag: String?
            var text: String?
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.name
                when (event) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "Магазин") {
                            localShop = HashMap()
                            for (i in 0 until parser.attributeCount) {
                                localShop[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        if (tag == "Пользователь") {
                            localUser = HashMap()
                            for (i in 0 until parser.attributeCount) {
                                localUser[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        text = parser.text
                        Timber.d(text)
                    }
                    XmlPullParser.END_TAG -> when (tag) {
                        "Магазин" -> {
                            if (localShop["Адрес"]?.contains("TSD") == true) {
                                localShopList.add(localShop)
                            }
                        }
                        "Пользователь" -> localUserList.add(localUser)
                    }
                }
                event = parser.next()
            }
        } catch (e: IOException) {
            Timber.e(e)
        } catch (e: XmlPullParserException) {
            Timber.e(e)
        }

        Common.shopsArray.clear()
        localShopList.forEach {
            val newShop = Shop(
                it["Наименование"].toString(),
                it["ПрефиксМагазина"].toString(),
                it["ПрефиксШтучногоТовара"].toString(),
                it["ПрефиксВесовогоТовара"].toString(),
                it["ПрефиксВесовогоТовараПЛУ"].toString(),
                it["Адрес"].toString()
            )
            Common.shopsArray.add(newShop)
        }
        Common.usersArray.clear()
        localUserList.forEach {
            val newUser = GroupUser(
                it["Логин"].toString(),
                it["Пароль"].toString()
            )
            Common.usersArray.add(newUser)
        }
    }

    private lateinit var viewModel: LoginViewModel
    private fun loadSettings() {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        viewModel.usersLiveData.observe(this) {
            Timber.d(it.toString())
        }
    }

    private fun loadSettings1() {
        val repository = RepositorySettings(retrofitService)
        val response = repository.getSettings()

        response.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    FancyToast.makeText(
                        applicationContext,
                        "Файл XML получен",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.SUCCESS, false
                    ).show()
                    response.body()?.let {
                        parse1C(it)
                    }
                } else {
                    FancyToast.makeText(
                        applicationContext,
                        "Файл XML не получен",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING, false
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                FancyToast.makeText(
                    applicationContext,
                    "Ошибка получения настроек ${t.message}",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }
}