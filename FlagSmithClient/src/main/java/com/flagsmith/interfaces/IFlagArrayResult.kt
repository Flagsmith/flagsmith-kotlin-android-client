package com.flagsmith.interfaces

import com.flagsmith.response.ResponseFlagElement

interface IFlagArrayResult {

    fun success( list: ArrayList<ResponseFlagElement>)
    fun failed(str : String )

}