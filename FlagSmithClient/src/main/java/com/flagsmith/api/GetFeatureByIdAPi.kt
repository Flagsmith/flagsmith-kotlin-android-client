package com.flagsmith.api

import com.flagsmith.builder.FlagsmithBuilder
//import com.flagsmith.flagsmith.android.flagsmith.interfaces.IFeatureFoundChecker
import com.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.interfaces.IFlagSingle
import com.flagsmith.response.ResponseFlagElement

class GetFeatureByIdAPi(builder: FlagsmithBuilder, searchText : String, finish: IFlagSingle) {

    var builder: FlagsmithBuilder
    var searchText : String
    var finish : IFlagSingle
    var resultList :  ArrayList<ResponseFlagElement> = ArrayList()

    init {
        this.searchText = searchText
        this.finish = finish
        this.builder = builder

        startAPI()
    }


    private fun startAPI() {
        GetFlagAllAPi(   builder, object : IFlagArrayResult {
            override fun success(list: ArrayList<ResponseFlagElement>) {
                resultList = list

                 searchInResult()
            }

            override fun failed(str: String) {
                finish.failed( str )
            }

        })
    }


    private fun searchInResult() {

        //check empty
        if(resultList.size == 0){
            finish.failed( "Not Found")
            return
        }

        //search
        for ( m in resultList ) {
            //case: found result
            if( m.feature.name == searchText ) {
                foundResult( m )
                return
            }
        }

        //case: not found result
        finish.failed( "Not Found")
    }

    private fun foundResult(m: ResponseFlagElement) {
        finish.success( m  )
    }


}