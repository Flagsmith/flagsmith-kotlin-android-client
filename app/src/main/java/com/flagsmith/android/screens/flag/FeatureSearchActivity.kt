package com.flag.android.screens.flag

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.Flagsmith
import com.flagsmith.entities.Flag
import com.flagsmith.android.R
import com.flagsmith.android.adapter.FlagAdapter
import com.flagsmith.android.adapter.FlagPickerSelect

import com.flagsmith.android.helper.Helper

class FeatureSearchActivity : AppCompatActivity() {

    lateinit var activity: Activity
    lateinit var ed_search_feature : EditText
    lateinit var bt_searchFeature : Button
    lateinit var tv_result_searchFeature : TextView
    lateinit var prg_pageFeatureSearch : ProgressBar
    lateinit var rv_featureResult : RecyclerView

    lateinit var flagsmith : Flagsmith

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_search)

        this.activity = this

        //find by ids
        this.ed_search_feature = findViewById(R.id.ed_search_feature)
        this.bt_searchFeature = findViewById(R.id.bt_searchFeature)
        this.tv_result_searchFeature = findViewById(R.id.tv_result_searchFeature)
        this.prg_pageFeatureSearch = findViewById(R.id.prg_pageFeatureSearch)
        this.rv_featureResult = findViewById(R.id.rv_featureResult)

        initBuilder()

        //buttons
        setupButtonSearch()
    }

    private fun initBuilder() {
        flagsmith = Flagsmith(environmentKey = Helper.environmentDevelopmentKey, context = baseContext)
    }

    private fun setupButtonSearch() {
        bt_searchFeature.setOnClickListener{

            //validate
            val entry = ed_search_feature.text.toString()
            if(entry.isEmpty()) {
                Toast.makeText(this, "Enter Feature Identifier", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //api
            apiSearch( entry )

        }
    }

    private fun apiSearch( searchText : String ) {
        //progress
        prg_pageFeatureSearch.visibility = View.VISIBLE
        rv_featureResult.visibility = View.GONE

        //keybaord
        Helper.keyboardHidden( activity )

        flagsmith.getFeatureFlags(Helper.identity) { result ->
            Helper.callViewInsideThread( activity) {
                prg_pageFeatureSearch.visibility = View.GONE
                result.fold(
                    onSuccess = { flags ->
                        val flag = flags.find { flag -> flag.feature.name == searchText }
                        if (flag != null) {
                            createAdapterFlag(flag)
                        } else {
//                            Toast.makeText(this@FeatureSearchActivity, "Couldn't find feature", Toast.LENGTH_SHORT)
//                                .show()
                        }
                    },
                    onFailure = { e ->
                        Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Also check with hasFeatureFlag to ensure that we're clocking the search on the analytics
        flagsmith.hasFeatureFlag(searchText, Helper.identity) { result ->
            Helper.callViewInsideThread( activity) {
                result.fold(
                    onSuccess = { result ->
                        if (result) {
                            Toast.makeText(this@FeatureSearchActivity, "Feature found", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@FeatureSearchActivity, "Couldn't find feature with hasFeatureFlag", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { e ->
                        Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }


    private fun createAdapterFlag( flag: Flag) {
        val list: ArrayList<Flag> = ArrayList()
        list.add( flag )

        val manager = LinearLayoutManager( activity )
        manager.orientation = LinearLayoutManager.VERTICAL
        rv_featureResult.layoutManager = manager
        val customAdapter = FlagAdapter(  activity , list, object : FlagPickerSelect {
            override fun click(favContact: Flag?) {

            }
        })

        rv_featureResult.adapter = customAdapter
        rv_featureResult.visibility = View.VISIBLE
    }


    override fun onResume() {
        super.onResume()
        //keyboard
        Helper.keyboardHidden( activity )
    }

    ///--------------------------------------------- get the flag single data

}