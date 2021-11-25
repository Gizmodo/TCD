package com.shop.tcd

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.utils.Common
import com.shop.tcd.databinding.ActivityLoginBinding
import com.shop.tcd.repository.Repository
import com.shop.tcd.repository.RetrofitService
import com.shop.tcd.model.settings.Group
import com.shop.tcd.model.settings.GroupUser
import com.shop.tcd.model.settings.Settings
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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