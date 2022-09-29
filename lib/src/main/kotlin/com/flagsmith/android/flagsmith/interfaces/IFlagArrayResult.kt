package com.flagsmith.android.flagsmith.interfaces

import com.flagsmith.android.flagsmith.response.ResponseFlagElement

interface IFlagArrayResult {

    fun success( list: ArrayList<ResponseFlagElement>);
    fun failed(str : String );

}