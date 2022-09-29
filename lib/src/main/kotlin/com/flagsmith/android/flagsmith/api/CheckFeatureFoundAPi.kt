package com.flagsmith.android.flagsmith.api

import com.flagsmith.android.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.android.flagsmith.interfaces.IFeatureFoundChecker
import com.flagsmith.android.flagsmith.interfaces.IFlagSingle
import com.flagsmith.android.flagsmith.response.ResponseFlagElement

class CheckFeatureFoundAPi(builder: FlagsmithBuilder, searchText : String, finish: IFeatureFoundChecker) {

//    var context : Context;
    var builder: FlagsmithBuilder;
    var searchText : String;
    var finish : IFeatureFoundChecker;

    init {
//        this.context = context;
        this.searchText = searchText;
        this.finish = finish;
        this.builder = builder;

        startAPI();
    }


    private fun startAPI() {
        GetFeatureByIdAPi(   builder, searchText, object : IFlagSingle {
            override fun success(flag: ResponseFlagElement) {

                 finish.found()
            }

            override fun failed(str: String) {
                finish.notFound()
            }

        });
    }




}