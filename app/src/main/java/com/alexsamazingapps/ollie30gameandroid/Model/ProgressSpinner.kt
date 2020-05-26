package com.alexsamazingapps.ollie30gameandroid.Model

import android.app.ActionBar
import android.support.constraint.Constraints
import android.text.Layout
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*

class ProgressSpinner {

    fun show(progressBar: ProgressBar, window: Window) {
        progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun hide(progressBar: ProgressBar, window: Window) {
        progressBar.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}