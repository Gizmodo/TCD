package com.shop.tcd.ui.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
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
import timber.log.Timber

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val binding by viewBindingWithBinder(FragmentLoginBinding::bind)
    private lateinit var usersList: UsersList
    private val viewModel: LoginViewModel by lazy {
        getViewModel { LoginViewModel() }
    }

    private fun setStateUI(enabled: Boolean) = with(binding) {
        if (!enabled) {
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
        } else {
            imageView.clearAnimation()
        }
        tilLogin.isFocusableInTouchMode = enabled
        tilPassword.isFocusableInTouchMode = enabled
        btnLogin.isEnabled = enabled
        edtLogin.setReadOnly(!enabled)
        edtPassword.setReadOnly(!enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showIPAddress()
        showVPNUsage()

        initUIListener()
        setStateUI(enabled = false)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewModel.usersLiveData.observe(viewLifecycleOwner) {
            Timber.d(it.toString())
            usersList = it
            setupLogins(binding.edtLogin, it)
        }

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyError { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                setStateUI(enabled = false)
            } else {
                setStateUI(enabled = true)
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
                longFancy { "Неверно указаны данные для входа" }
            }
        }
    }

    private fun showVPNUsage() {
        when {
            vpnActive() -> {
                binding.txtVPNStatus.text = "VPN"
            }
            else -> {
                binding.txtVPNStatus.text = "No VPN"
            }
        }
    }

    private fun vpnActive(): Boolean {
        var vpnInUse = false
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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