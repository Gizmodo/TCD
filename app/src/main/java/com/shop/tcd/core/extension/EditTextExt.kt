package com.shop.tcd.core.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.CheckResult
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.TimeUnit

private val TIMEOUT_KEYBOARD: Long = 50

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = (object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                trySend(s)
            }
        }).apply {
            addTextChangedListener(this)
        }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun EditText.setReadOnly(value: Boolean) {
//        isFocusable = !value
    isEnabled = !value
//        isFocusableInTouchMode = !value
}

fun toObservable(editText: TextInputEditText): Observable<String> {
    val observable = Observable.create<String> { emitter ->
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.toString()?.let { emitter.onNext(it) }
            }

            override fun afterTextChanged(p0: Editable?) {}
        }
        editText.addTextChangedListener(textWatcher)
        emitter.setCancellable {
            editText.removeTextChangedListener(textWatcher)
        }
    }
    return observable.debounce(TIMEOUT_KEYBOARD, TimeUnit.MILLISECONDS)
}
