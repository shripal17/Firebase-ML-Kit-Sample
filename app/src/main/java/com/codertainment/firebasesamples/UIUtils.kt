package com.codertainment.firebasesamples

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object UIUtils {
  fun Activity.setBarTranslucent(statusBar: Boolean, navigationBar: Boolean = false) {
    val window = this.window

    val statusBarTranslucency = if (statusBar) {
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    } else {
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    }

    val navBarTranslucency = if (navigationBar) {
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    } else {
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    }

    if (statusBar || navigationBar) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      window.setFlags(statusBarTranslucency, navBarTranslucency)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
  }

  fun hideKeyboard(activity: AppCompatActivity) {
    val v = activity.currentFocus
    getIMM(activity).hideSoftInputFromWindow(v.windowToken, 0)
  }

  fun showKeyboard(editText: EditText) {
    editText.requestFocus()
    getIMM(editText.context).showSoftInput(editText, InputMethodManager.SHOW_FORCED)
  }

  fun getIMM(ctx: Context) = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}