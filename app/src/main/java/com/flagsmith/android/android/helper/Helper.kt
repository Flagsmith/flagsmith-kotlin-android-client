package com.flagsmith.android.android.helper

import android.app.Activity
import android.os.Handler


object Helper {

    var tokenApiKey: String = ""
    var environmentDevelopmentKey = ""
    var identifierUserKey: String = ""

    fun callViewInsideThread( activity :Activity, myUnit : ( )-> Unit  ){
        //run UI inside thread
        val h = Handler( activity.mainLooper)
        h.post {
            myUnit()
        }
    }





}