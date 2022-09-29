package com.flagsmith.android.flagsmith.interfaces

import com.flagsmith.android.flagsmith.response.ResponseTraitUpdate

interface ITraitUpdate {

    fun success( response: ResponseTraitUpdate);
    fun failed(str : String );

}