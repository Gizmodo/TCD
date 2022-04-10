package com.shop.tcd.v2.core.extension

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shashank.sony.fancytoastlib.FancyToast

inline fun Fragment.longFancy(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
    .apply {
        show()
    }

inline fun Fragment.fancyInfo(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_LONG, FancyToast.INFO, false)
    .apply {
        show()
    }

inline fun Fragment.shortFancy(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_SHORT, FancyToast.INFO, false)
    .apply {
        show()
    }

inline fun Fragment.longFancyConfusing(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_LONG, FancyToast.CONFUSING, false)
    .apply {
        show()
    }

inline fun Fragment.fancySuccess(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false)
    .apply {
        show()
    }

inline fun Fragment.fancyError(message: () -> String): Toast = FancyToast
    .makeText(this.context, message(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
    .apply {
        show()
    }