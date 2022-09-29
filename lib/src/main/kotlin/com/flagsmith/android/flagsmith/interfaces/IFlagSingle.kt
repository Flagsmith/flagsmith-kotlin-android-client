package com.flagsmith.android.flagsmith.interfaces

import com.flagsmith.android.flagsmith.response.ResponseFlagElement

interface IFlagSingle {

    fun success( flag: ResponseFlagElement);
    fun failed(str : String );

}