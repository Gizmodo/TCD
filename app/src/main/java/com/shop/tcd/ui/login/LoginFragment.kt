package com.shop.tcd.ui.login

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shop.tcd.BuildConfig
import com.shop.tcd.R
import com.shop.tcd.core.extension.*
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_FROM_DEGREE
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_PIVOT
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_TIMEOUT
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_TO_DEGREE
import com.shop.tcd.core.utils.Constants.SelectedObjects.UserModel
import com.shop.tcd.core.utils.Constants.SelectedObjects.UserModelPosition
import com.shop.tcd.data.dto.user.UserModel
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val binding by viewBindingWithBinder(FragmentLoginBinding::bind)
    private lateinit var usersList: UsersList
    private val viewModel: LoginViewModel by lazy {
        getViewModel { LoginViewModel() }
    }

    private fun showAnimation() = with(binding) {
        val rotate = RotateAnimation(
            ANIMATION_FROM_DEGREE,
            ANIMATION_TO_DEGREE,
            Animation.RELATIVE_TO_SELF,
            ANIMATION_PIVOT,
            Animation.RELATIVE_TO_SELF,
            ANIMATION_PIVOT
        )
        rotate.duration = ANIMATION_TIMEOUT
        rotate.repeatMode = Animation.INFINITE
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = AccelerateDecelerateInterpolator()
        imageView.startAnimation(rotate)
    }

    private fun hideAnimation() {
        binding.imageView.clearAnimation()
    }

    private fun setStateUI(enabled: Boolean) = with(binding) {
        tilLogin.isFocusableInTouchMode = enabled
        tilPassword.isFocusableInTouchMode = enabled
        btnLogin.isEnabled = enabled
        edtLogin.setReadOnly(!enabled)
        edtPassword.setReadOnly(!enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showIPAddress()
        showBuildVersion()

        initUIListener()
        setStateUI(false)
        showAnimation()
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.loadOptions()
        }

        viewModel.usersLiveData.observe(viewLifecycleOwner) {
            usersList = it
            setStateUI(usersList.size > 0)
            setupLogins(binding.edtLogin, it)
        }

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            hideAnimation()
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            hideAnimation()
            fancyError { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when (it) {
                false -> hideAnimation()
                true -> showAnimation()
            }
        }
    }

    private fun initUIListener() {
        binding.tilLogin.apply {
            setOnFocusChangeListener { _, _ -> this@LoginFragment.hideSoftKeyboardExt() }
        }

        binding.tilPassword.apply {
            setOnFocusChangeListener { _, _ -> this@LoginFragment.hideSoftKeyboardExt() }
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnOptions.setOnClickListener {
            viewModel.job?.cancel()
            navigateExt(LoginFragmentDirections.actionLoginFragmentToOptionsFragment())
        }
    }

    private fun authenticate(
        selectedUser: UserModel,
        userName: String,
        userPassword: String,
    ): Boolean {
        val result = selectedUser.name == userName && selectedUser.password == userPassword
        if (result) {
            UserModel = selectedUser
        }
        return result
    }

    private fun login() {
        val userName = binding.edtLogin.text.toString()
        val userPassword = binding.edtPassword.text.toString()
        var isSuccess = false
        usersList.forEach {
            isSuccess = isSuccess || authenticate(it, userName, userPassword)
        }
        when {
            isSuccess -> {
                navigateExt(LoginFragmentDirections.actionLoginFragmentToMainFragment())
            }
            else -> {
                fancyError { "Неверно указаны данные для входа" }
            }
        }
    }

    private fun showBuildVersion() {
        binding.txtVPNStatus.text = BuildConfig.VERSION_NAME
    }

    private fun showIPAddress() {
        val wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        binding.txtIPAddress.text = ipAddress
    }

    private fun setupLogins(view: AutoCompleteTextView, items: UsersList) {
        val names: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return items[index].name
            }

            override val size: Int
                get() = items.size
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            UserModel = items[position]
            UserModelPosition = position
        }
        if (UserModelPosition != -1) {
            view.setText(adapter.getItem(UserModelPosition), false)
        }
    }
}
