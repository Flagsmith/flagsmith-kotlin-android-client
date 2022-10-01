package com.flagsmith.android.android.screens

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.flagmsith.R
import com.flagsmith.android.android.screens.flag.FeatureSearchActivity
import com.flagsmith.android.android.screens.flag.FlagListActivity
import com.flagsmith.android.android.screens.trait.TraitsActivity

class ChoosePageActivity : AppCompatActivity() {

    lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_page)
        activity = this

        setup_bt_flags()
        setup_bt_feature()
        setup_bt_trait()
    }

    private fun setup_bt_flags() {
        val bt_flags : Button = findViewById(R.id.bt_flags)
        bt_flags.setOnClickListener{
            Log.i( "abdo","setup_bt_flags() - click ")

            val i = Intent( activity, FlagListActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun setup_bt_feature() {
        val bt_feature : Button = findViewById(R.id.bt_feature)
        bt_feature.setOnClickListener{
            val i = Intent( activity, FeatureSearchActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun setup_bt_trait() {
        val bt_trait : Button = findViewById(R.id.bt_trait)
        bt_trait.setOnClickListener{

            val i = Intent( activity, TraitsActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)

        }
    }

}