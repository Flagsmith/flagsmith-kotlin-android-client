package com.flagsmith.interfaces

import com.flagsmith.response.Flag

interface IFlagArrayResult {
    fun success(list: ArrayList<Flag>)
    fun failed(str: String)
}