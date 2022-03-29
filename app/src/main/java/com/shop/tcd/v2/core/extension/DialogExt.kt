package com.shop.tcd.v2.core.extension

import android.content.DialogInterface
import android.widget.ArrayAdapter
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.showSelectDialogExt(
    list: List<T>,
    @StringRes titleId: Int,
    noinline listener: (di: DialogInterface, i: Int) -> Unit,
): AlertDialog {
    val rcontext = requireContext()
    val dialog = AlertDialog.Builder(rcontext)
        .setTitle(titleId)
        .setAdapter(
            ArrayAdapter(
                rcontext,
                android.R.layout.select_dialog_item,
                list
            ),
            listener
        )
        .create()
    dialog.show()
    return dialog
}