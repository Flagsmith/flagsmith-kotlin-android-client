package com.flagsmith.android.flagsmith.interfaces

import com.flagsmith.android.flagsmith.response.Trait

interface ITraitArrayResult {

    fun success( list: ArrayList<Trait>);
    fun failed(str : String );

}