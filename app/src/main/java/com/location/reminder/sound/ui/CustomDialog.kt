package com.location.reminder.sound.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.location.reminder.sound.R

class CustomDialog(
    context: Context,
    private val title: String,
    private val subTitle: String,
    private val primaryActionTitle: String = "Ok",
    private val secondaryActionTitle: String = "Cancel",
    private val callback: () -> Unit
) :
    Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dailog_custom)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        txtTitle.text = title
        val txtSubTitle = findViewById<TextView>(R.id.txtSubTitle)
        txtSubTitle.text = subTitle
        val txtAllow = findViewById<TextView>(R.id.txtAllow)
        txtAllow.text = primaryActionTitle
        val txtDeny = findViewById<TextView>(R.id.txtDeny)
        txtDeny.text = secondaryActionTitle

        txtAllow.setOnClickListener {
            callback()
            dismiss()
        }
        txtDeny.setOnClickListener {
            dismiss()
        }
    }
}