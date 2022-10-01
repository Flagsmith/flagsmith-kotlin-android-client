package com.flagsmith.interfaces

import com.flagsmith.response.ResponseFlagElement

interface IFlagSingle {

    fun success( flag: ResponseFlagElement)
    fun failed(str : String )

}