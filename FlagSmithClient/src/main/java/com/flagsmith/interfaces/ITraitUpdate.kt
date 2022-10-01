package com.flagsmith.interfaces

import com.flagsmith.response.ResponseTraitUpdate

interface ITraitUpdate {

    fun success( response: ResponseTraitUpdate)
    fun failed(str : String )

}