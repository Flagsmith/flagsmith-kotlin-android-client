package com.flagsmith.android.android.screens.flag

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.android.android.adapter.FlagAdapter
import com.flagsmith.android.android.adapter.FlagPickerSelect
import com.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.response.ResponseFlagElement
import com.flagsmith.android.android.toolbar.ToolbarSimple
import com.flagmsith.R
import com.flagsmith.android.helper.Helper

class FlagListActivity : AppCompatActivity() {

    lateinit var flagBuilder : FlagsmithBuilder

    lateinit var activity: Activity
    lateinit var context : Context

    lateinit var rv_flags : RecyclerView

    lateinit var prg_flags : ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flag_list)

        activity = this
        context = this

        //find by ids
        rv_flags = findViewById(R.id.rv_flags)
        prg_flags = findViewById(R.id.prg_flags)

        initBuilder()

        setupToolbar()

    }

    private fun setupToolbar() {
        ToolbarSimple( this, R.id.toolbarFlagList, "Flags")
    }


    private fun initBuilder() {
        flagBuilder = FlagsmithBuilder.Builder()
            .tokenApi( Helper.tokenApiKey)
            .environmentId(Helper.environmentDevelopmentKey)
            .identifierUser( Helper.identifierUserKey)
            .build()
    }


    private fun getAllData() {

        //progress
        prg_flags.visibility = View.VISIBLE

        //listener
        flagBuilder.getAllFlag(   object : IFlagArrayResult {
            override fun success(list: ArrayList<ResponseFlagElement>) {

                Helper.callViewInsideThread( activity) {
                    //progress
                    prg_flags.visibility = View.GONE

                    //check size
                    if (list.size == 0) {
                        Toast.makeText(this@FlagListActivity, "No Data Found", Toast.LENGTH_SHORT)
                            .show()
                        return@callViewInsideThread
                    }

                    //list
                    createAdapterFlag(list)
                }

            }

            override fun failed(str: String) {

                Helper.callViewInsideThread( activity) {
                    //progress
                    prg_flags.visibility = View.GONE

                    //toast
                    Toast.makeText(activity, str, Toast.LENGTH_SHORT).show()
                }


            }
        })
    }


    private fun createAdapterFlag(list: ArrayList<ResponseFlagElement>) {
        val manager = LinearLayoutManager(context )
        manager.orientation = LinearLayoutManager.VERTICAL
        rv_flags.layoutManager = manager
        val customAdapter = FlagAdapter(  context , list, object : FlagPickerSelect{
            override fun click(favContact: ResponseFlagElement?) {

            }
        })
        rv_flags.adapter = customAdapter
    }


    override fun onResume() {
        super.onResume()

        getAllData()
    }

}