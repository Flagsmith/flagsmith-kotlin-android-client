package com.flagsmith.interfaces

import com.flagsmith.response.ResponseTraitUpdate

interface IIdentity {
    fun success( response: ResponseTraitUpdate)
    fun failed(str : String )

}