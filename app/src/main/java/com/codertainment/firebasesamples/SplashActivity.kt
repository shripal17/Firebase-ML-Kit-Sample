package com.codertainment.firebasesamples

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.codertainment.firebasesamples.UIUtils.setBarTranslucent
import com.github.florent37.runtimepermission.kotlin.askPermission
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setBarTranslucent(true, true)
  }

  override fun onResume() {
    super.onResume()

    askPermission() {
      if (!it.isAccepted) {
        toast("Please grant camera permission to continue")
        it.goToSettings()
      } else {
        Handler().postDelayed({
          startActivity<MainActivity>()
          finish()
        }, 2000)
      }
    }
  }
}
