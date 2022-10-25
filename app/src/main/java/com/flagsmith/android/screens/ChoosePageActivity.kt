package com.flagsmith.android.screens

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.flag.android.screens.flag.FeatureSearchActivity
import com.flagmsith.android.R
import com.flagsmith.android.screens.flag.FlagListActivity
import com.flagsmith.android.screens.trait.TraitsActivity

class ChoosePageActivity : AppCompatActivity() {

    lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_page)
        activity = this

        setupBtFlags()
        setupBtFeature()
        setupBtTrait()
    }

    private fun setupBtFlags() {
        val btFlags : Button = findViewById(R.id.bt_flags)
        btFlags.setOnClickListener{
            val i = Intent( activity, FlagListActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun setupBtFeature() {
        val btFeature : Button = findViewById(R.id.bt_feature)
        btFeature.setOnClickListener{
            val i = Intent( activity, FeatureSearchActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun setupBtTrait() {
        val btTrait : Button = findViewById(R.id.bt_trait)
        btTrait.setOnClickListener{

            val i = Intent( activity, TraitsActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

}