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
import com.shop.tcd.model.settings.Settings
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
        loadXml()
    }

    /** Кнопка Войти */
    fun btnLogin(@Suppress("UNUSED_PARAMETER") view: View) {
        val userName = binding.edLogin.text.toString()
        val userPassword = binding.edPassword.text.toString()
        var isSuccess = false
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "empty")
        }
        groups?.forEach { grp ->
            grp.groupUsers.forEach(fun(it: GroupUser) {
                val isFound = check(it, userName, userPassword)
                isSuccess = isSuccess || isFound
            })
        }

        when {
            isSuccess -> {
                startActivity(intent)
            }
            else -> {
                FancyToast.makeText(applicationContext,
                    "Неверно указаны данные для входа",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false).show()
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

    private fun parse1C() {
        val tg = "parse"

        // creating a user list string hash map arraylist
        val shopList = ArrayList<HashMap<String, String?>>()
        val userList = ArrayList<HashMap<String, String?>>()
        val groupList = ArrayList<HashMap<String, ArrayList<HashMap<String, String?>>>>()
        try {


            // creating a user string hashmap
            var user = HashMap<String, String?>()
            var shop = HashMap<String, String?>()
            var group = HashMap<String, ArrayList<HashMap<String, String?>>>()
            // input stream the userdetails.xml file
            val istream = assets.open("1c.xml")

            //creating a XmlPull parse Factory instance
            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()

            // setting the namespaces feature to false
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            // setting the input to the parser
            parser.setInput(istream, null)

            // working with the input stream
            var tag: String? = ""
            var text: String? = ""
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.name
                when (event) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "Магазин") {
                            shop = HashMap()
                            var tmp = ""
                            for (i in 0 until parser.attributeCount) {
                                tmp = (tmp + parser.getAttributeName(i).toString() + " = "
                                        + parser.getAttributeValue(i).toString() + ", ")
                                Log.d(tg, tmp)
                                shop[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        if (tag == "Группа") {
                            group = HashMap()
                            for (i in 0 until parser.attributeCount) {
                                group[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        if (tag == "Пользователь") {
                            user = HashMap()
                            for (i in 0 until parser.attributeCount) {
                                user[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }
                        }
                        Log.d(tg, "START_TAG: name = " + parser.name
                                + ", depth = " + parser.depth + ", attrCount = "
                                + parser.attributeCount);
                    }
                    XmlPullParser.TEXT -> {
                        text = parser.text
                        Log.d(tg, text)
                    }
                    XmlPullParser.END_TAG -> when (tag) {
                        "Магазин" -> shopList.add(shop)
                        "Пользователь" -> userList.add(user)
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
        Log.d(tg, "finish parsing xml")
    }

    private fun test() {
        try {

            // creating a user list string hash map arraylist
            val userList = ArrayList<HashMap<String, String?>>()

            // creating a user string hashmap
            var user = HashMap<String, String?>()

            // input stream the userdetails.xml file
            val istream = assets.open("userdetails.xml")

            //creating a XmlPull parse Factory instance
            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()

            // setting the namespaces feature to false
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            // setting the input to the parser
            parser.setInput(istream, null)

            // working with the input stream
            var tag: String? = ""
            var text: String? = ""
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.name
                when (event) {
                    XmlPullParser.START_TAG -> if (tag == "user") user = HashMap()
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> when (tag) {
                        "name" -> user["name"] = text
                        "designation" -> user["designation"] = text
                        "user" -> userList.add(user)
                    }
                }
                event = parser.next()
            }

            // List Adapter to broadcast the information to the list_rows.xml file
            /*  val adapter: ListAdapter = SimpleAdapter(this, userList, R.layout.list_row,
                  arrayOf("name", "designation"), intArrayOf(R.id.name, R.id.designation)
              )
              lv.adapter = adapter*/
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
    }

    private fun loadXml() {
        Log.e("TAG", "loadXml")
        val repository = Repository(retrofitService)
        val response = repository.getxml()

        response.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                response.body()?.let {
                    Log.d("TAG", it)
//                    test()
                    parse1C()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("TAG", t.message.toString())
            }
        })
    }

    /**
     * Загружает настройки
     */
    private fun loadSettings() {
        val repository = Repository(retrofitService)
        val response = repository.getSettings()

        response.enqueue(object : Callback<Settings> {
            override fun onResponse(call: Call<Settings>, response: Response<Settings>) {
                if (response.isSuccessful) {
                    FancyToast.makeText(applicationContext,
                        "Успешный ответ",
                        FancyToast.LENGTH_LONG,
                        FancyToast.SUCCESS,
                        false).show()
                    groups = response.body()?.settings?.groups
                    Common.shopsList = response.body()?.settings?.shops
                } else {
                    FancyToast.makeText(applicationContext,
                        "Ответ не успешный",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false).show()
                }
            }

            override fun onFailure(call: Call<Settings>, t: Throwable) {
                FancyToast.makeText(applicationContext,
                    "Ошибка получения настроек ${t.message.toString()}",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show()
            }
        })
    }
}