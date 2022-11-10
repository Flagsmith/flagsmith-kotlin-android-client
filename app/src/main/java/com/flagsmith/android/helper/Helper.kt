package com.flagsmith.android.helper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager


object Helper {
    var environmentDevelopmentKey = "F5X4CN67ZmSB547j2k2nX4" // System.getenv("ENVIRONMENT_KEY")
    var identity: String = "person"

    fun keyboardHidden(mActivity: Activity) {
        try {
            val inputManager: InputMethodManager =
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                mActivity.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (_: Exception) {

        }
        try {
            mActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        } catch (_: Exception) {
        }
    }


    fun callViewInsideThread( activity :Activity, myUnit : ( )-> Unit  ){

        //run UI inside thread
        val h = Handler( activity.mainLooper)
        h.post {
            myUnit()
        }
    }
}