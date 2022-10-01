package com.flagsmith.api

import com.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.interfaces.IFeatureFoundChecker
import com.flagsmith.interfaces.IFlagSingle
import com.flagsmith.response.ResponseFlagElement

class CheckFeatureFoundAPi(builder: FlagsmithBuilder, searchText : String, finish: IFeatureFoundChecker) {

    var builder: FlagsmithBuilder
    var searchText : String
    var finish : IFeatureFoundChecker

    init {
        this.searchText = searchText
        this.finish = finish
        this.builder = builder

        startAPI()
    }


    private fun startAPI() {
        GetFeatureByIdAPi(   builder, searchText, object : IFlagSingle {
            override fun success(flag: ResponseFlagElement) {

                 finish.found()
            }

            override fun failed(str: String) {
                finish.notFound()
            }

        })
    }




}