package com.shop.tcd.ui.login

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shop.tcd.BuildConfig
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.hideSoftKeyboardExt
import com.shop.tcd.core.extension.navigateExt
import com.shop.tcd.core.extension.setReadOnly
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_FROM_DEGREE
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_PIVOT
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_TIMEOUT
import com.shop.tcd.core.utils.Constants.Animation.ANIMATION_TO_DEGREE
import com.shop.tcd.core.utils.Constants.SelectedObjects.UserModel
import com.shop.tcd.core.utils.Constants.SelectedObjects.UserModelPosition
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.user.UserModel
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.databinding.FragmentLoginBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

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
        binding.txtVersion.text = BuildConfig.VERSION_NAME
        initUIListener()
        setStateUI(false)
        showAnimation()
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadOptions()
                viewModel.state.collectLatest {
                    when (it) {
                        is StatefulData.Error -> {
                            hideAnimation()
                            fancyError { it.msg }
                        }
                        StatefulData.Loading -> {
                            Timber.d("Запрос на получение обновлений")
                            showAnimation()
                        }
                        is StatefulData.Notify -> {
                            hideAnimation()
                            fancyException { it.msg }
                        }
                        is StatefulData.Success -> {
                            hideAnimation()
                            usersList = it.result
                            setStateUI(usersList.size > 0)
                            setupLogins(binding.edtLogin, it.result)
                        }
                        StatefulData.Empty -> {}
                    }
                }
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
        if (result) UserModel = selectedUser
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
