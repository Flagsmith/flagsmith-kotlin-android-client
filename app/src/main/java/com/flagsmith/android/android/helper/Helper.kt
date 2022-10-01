package com.flagsmith.android.android.helper

import android.app.Activity
import android.os.Handler


object Helper {

    var tokenApiKey: String = "b97c6f022fe7b736f7bcf6d99019337a7ff2f7d3"
    var environmentDevelopmentKey = "NaeCHAMjZtSmNudzhV9TWy"
    var identifierUserKey: String = "development_user_123456"


    fun callViewInsideThread( activity :Activity, myUnit : ( )-> Unit  ){

        //run UI inside thread
        val h = Handler( activity.mainLooper)
        h.post {
            myUnit()
        }
    }





}