package com.flagsmith.interfaces

import com.flagsmith.response.Trait

interface ITraitArrayResult {
    fun success( list: ArrayList<Trait>)
    fun failed(str : String )
}