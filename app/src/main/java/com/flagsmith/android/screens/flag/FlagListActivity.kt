package com.flagsmith.android.screens.flag

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.builder.Flagsmith
import com.flagsmith.response.Flag
import com.flagmsith.android.R
import com.flagsmith.android.adapter.FlagAdapter
import com.flagsmith.android.adapter.FlagPickerSelect

import com.flagsmith.android.helper.Helper
import com.flagsmith.android.toolbar.ToolbarSimple


class FlagListActivity : AppCompatActivity() {

    lateinit var flagBuilder : Flagsmith

    lateinit var activity: Activity
    lateinit var context : Context;

    lateinit var rv_flags : RecyclerView;

    lateinit var prg_flags : ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flag_list)

        activity = this;
        context = this;

        //find by ids
        rv_flags = findViewById(R.id.rv_flags);
        prg_flags = findViewById(R.id.prg_flags)

        initBuilder()
        setupToolbar()
        checkWithHasFeatureFlag()
        checkWithGetValueForFeature()
    }

    private fun setupToolbar() {
        ToolbarSimple( this, R.id.toolbarFlagList, "Flags");
    }


    private fun initBuilder() {
        flagBuilder = Flagsmith.Builder()
            .apiAuthToken( Helper.tokenApiKey)
            .environmentKey(Helper.environmentDevelopmentKey)
            .context(context)
            .build();
    }

    private fun checkWithHasFeatureFlag() {
        println("************************* checkWithHasFeatureFlag *************************")
        flagBuilder.hasFeatureFlag("no-value") {
            println("hasFeatureFlag 'no-value' $it")
        }
        flagBuilder.hasFeatureFlag("not-found") {
            println("hasFeatureFlag 'not-found' $it")
        }
    }

    private fun checkWithGetValueForFeature() {
        println("************************* checkWithGetValueForFeature *************************")
        flagBuilder.getValueForFeature("no-value") {
            println("getValueForFeature 'no-value' $it")
        }
        flagBuilder.getValueForFeature("not-found") {
            println("getValueForFeature 'not-found' $it")
        }
        flagBuilder.getValueForFeature("with-value") {
            println("getValueForFeature 'with-value' $it")
        }
    }

    private fun getAllData() {
        //progress
        prg_flags.visibility = View.VISIBLE

        //listener
        flagBuilder.getFeatureFlags(Helper.identity) { result ->
            Helper.callViewInsideThread( activity) {
                prg_flags.visibility = View.GONE

                result.fold(
                    onSuccess = { list ->
                        if (list.isEmpty()) {
                            Toast.makeText(this@FlagListActivity, "No Data Found", Toast.LENGTH_SHORT)
                                .show()
                            return@callViewInsideThread;
                        }

                        //list
                        createAdapterFlag(list);
                    },
                    onFailure = { e ->
                        Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun createAdapterFlag(list: List<Flag>) {
        val manager = LinearLayoutManager(context )
        manager.orientation = LinearLayoutManager.VERTICAL
        rv_flags.layoutManager = manager
        val customAdapter = FlagAdapter(  context , list, object : FlagPickerSelect{
            override fun click(favContact: Flag?) {

            }
        })
        rv_flags.adapter = customAdapter
    }


    override fun onResume() {
        super.onResume()

        getAllData();
    }

}