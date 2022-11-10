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
import com.flagsmith.Flagsmith
import com.flagsmith.entities.Flag
import com.flagsmith.android.R
import com.flagsmith.android.adapter.FlagAdapter
import com.flagsmith.android.adapter.FlagPickerSelect

import com.flagsmith.android.helper.Helper
import com.flagsmith.android.toolbar.ToolbarSimple


class FlagListActivity : AppCompatActivity() {

    lateinit var flagsmith : Flagsmith

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
    }

    private fun setupToolbar() {
        ToolbarSimple( this, R.id.toolbarFlagList, "Flags");
    }


    private fun initBuilder() {
        flagsmith = Flagsmith(environmentKey = Helper.environmentDevelopmentKey, context = context)
    }

    private fun getAllData() {
        //progress
        prg_flags.visibility = View.VISIBLE

        //listener
        flagsmith.getFeatureFlags(Helper.identity) { result ->
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