package com.flagsmith.interfaces

import com.flagsmith.response.Flag

interface IFlagSingle {
    fun success(flag: Flag)
    fun failed(str: String)
}