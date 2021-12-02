package com.shop.tcd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.ActivityLoginBinding
import com.shop.tcd.model.settings.Group
import com.shop.tcd.model.settings.GroupUser
import com.shop.tcd.model.settings.Shop
import com.shop.tcd.repository.Repository
import com.shop.tcd.repository.RetrofitService
import com.shop.tcd.utils.Common
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.StringReader

const val EXTRA_MESSAGE = "com.shop.tcd.MESSAGE"

class LoginActivity : AppCompatActivity() {
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var binding: ActivityLoginBinding
    private var groups: List<Group>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadSettings()
    }

    /** Кнопка Войти */
    fun btnLogin(@Suppress("UNUSED_PARAMETER") view: View) {
        val userName = binding.edLogin.text.toString()
        val userPassword = binding.edPassword.text.toString()
        var isSuccess = false
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "empty")
        }
        /*groups?.forEach { grp ->
            grp.groupUsers.forEach(fun(it: GroupUser) {
                val isFound = check(it, userName, userPassword)
                isSuccess = isSuccess || isFound
            })
        }*/

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
        val tg = "parse"
        // creating a user list string hash map arraylist
        val localShopList = ArrayList<HashMap<String, String?>>()
        val localuserList = ArrayList<HashMap<String, String?>>()
        try {
            // creating a user string hashmap
            var localUser = HashMap<String, String?>()
            var localShop = HashMap<String, String?>()
            // load xml from local file
            val istreamStringWOEncoding = StringReader(xmlResponseString)
            //creating a XmlPull parse Factory instance
            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()

            // setting the namespaces feature to false
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            // setting the input to the parser
            parser.setInput(istreamStringWOEncoding)
            // working with the input stream
            var tag: String?
            var text: String?
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.name
                when (event) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "Магазин") {
                            //DONE
                            localShop = HashMap()
                            //  var tmp = ""
                            for (i in 0 until parser.attributeCount) {
                                /*       tmp = (tmp + parser.getAttributeName(i).toString() + " = "
                                               + parser.getAttributeValue(i).toString() + ", ")
                                       Log.d(tg, tmp)*/
                                localShop[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        if (tag == "Пользователь") {
                            localUser = HashMap()
                            for (i in 0 until parser.attributeCount) {
                                localUser[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        /* Log.d(
                             tg, "START_TAG: name = " + parser.name
                                     + ", depth = " + parser.depth + ", attrCount = "
                                     + parser.attributeCount
                         );*/
                    }
                    XmlPullParser.TEXT -> {
                        text = parser.text
                        Log.d(tg, text)
                    }
                    XmlPullParser.END_TAG -> when (tag) {
                        "Магазин" -> localShopList.add(localShop)
                        "Пользователь" -> localuserList.add(localUser)
                        /* "name" -> user["name"] = text
                         "designation" -> user["designation"] = text
                         "user" -> userList.add(user)*/
                    }
                }
                event = parser.next()
            }


        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
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
        localuserList.forEach {
            val newUser = GroupUser(
                it["Логин"].toString(),
                it["Пароль"].toString()
            )
            Common.usersArray.add(newUser)
        }
    }

    /**
     * Загружает настройки
     */
    private fun loadSettings() {
        val repository = Repository(retrofitService)
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
                    "Ошибка получения настроек ${t.message.toString()}",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }
}