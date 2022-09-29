package com.flagsmith.android.flagsmith.api

import com.flagsmith.android.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.android.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.android.flagsmith.interfaces.IFlagSingle
import com.flagsmith.android.flagsmith.response.ResponseFlagElement

class GetFeatureByIdAPi(builder: FlagsmithBuilder, searchText : String, finish: IFlagSingle) {

    private var builder: FlagsmithBuilder;
    private var searchText : String;
    var finish : IFlagSingle;
    var resultList :  ArrayList<ResponseFlagElement> = ArrayList();

    init {
        this.searchText = searchText;
        this.finish = finish;
        this.builder = builder;

        startAPI();
    }


    private fun startAPI() {
        GetFlagAllAPi(   builder, object : IFlagArrayResult {
            override fun success(list: ArrayList<ResponseFlagElement>) {
                resultList = list;

                 searchInResult();
            }

            override fun failed(str: String) {
                finish.failed( str )
            }

        });
    }


    private fun searchInResult() {

        //check empty
        if(resultList.size == 0){
            finish.failed( "Not Found")
            return;
        }

        //search
        for ( m in resultList ) {
            if( m == null ) continue;
            if( m.feature == null ) continue;
            if( m.feature.name == null ) continue;

            //case: found result
            if( m.feature.name == searchText ) {
                foundResult( m );
                return;
            }
        }

        //case: not found result
        finish.failed( "Not Found")
    }

    private fun foundResult(m: ResponseFlagElement) {
        finish.success( m  );
    }


}