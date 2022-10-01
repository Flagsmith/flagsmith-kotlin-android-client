package com.flagsmith.android.android.screens.trait

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flagsmith.builder.FlagsmithBuilder
import com.flagsmith.interfaces.ITraitUpdate
import com.flagsmith.response.ResponseTraitUpdate
import com.flagmsith.R
import com.flagsmith.android.helper.Helper


class TraitCreateActivity : AppCompatActivity() {

    lateinit var activity  : Activity


    lateinit var flagBuilder : FlagsmithBuilder

    //views
    lateinit var prg_pageTraitCreate : ProgressBar
    lateinit var ed_trait_key : EditText
    lateinit var ed_trait_value : EditText
    lateinit var bt_save_trait : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trait_create)

        activity = this

        //find by id
        prg_pageTraitCreate = findViewById(R.id.prg_pageTraitCreate)
        ed_trait_key = findViewById(R.id.ed_trait_key)
        ed_trait_value = findViewById(R.id.ed_trait_value)
        bt_save_trait = findViewById(R.id.bt_save_trait)

        //init
        initBuilder()

        //ui
        setupButtonSave()

    }


    private fun initBuilder() {
        flagBuilder = FlagsmithBuilder.Builder()
            .tokenApi( Helper.tokenApiKey)
            .environmentId(Helper.environmentDevelopmentKey)
            .identifierUser( Helper.identifierUserKey)
            .build()
    }


    private fun setupButtonSave() {
        bt_save_trait.setOnClickListener {

            //keybaord
            Helper.keyboardHidden( activity )

            //validate
            val entryTraitKey = ed_trait_key.text.toString()
            if( entryTraitKey.isEmpty() ) {
                Toast.makeText(activity, "Trait Key Missed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //validate
            val entryTraitValue = ed_trait_value.text.toString()
            if( entryTraitValue.isEmpty() ) {
                Toast.makeText(activity, "Trait Value Missed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //api
            apiStart( entryTraitKey, entryTraitValue )


        }
    }

    private fun apiStart( key : String , value : String ) {

        //progress start
        prg_pageTraitCreate.visibility = View.VISIBLE

        //listener
        flagBuilder.createTrait(  key, value, object  : ITraitUpdate {
            override fun success(response: ResponseTraitUpdate) {


                Helper.callViewInsideThread( activity) {
                    //progress end
                    prg_pageTraitCreate.visibility = View.GONE

                    finishClassAfterSeeToast()
                }

            }

            override fun failed(str: String) {


                Helper.callViewInsideThread( activity) {
                    //progress end
                    prg_pageTraitCreate.visibility = View.GONE

                    Toast.makeText(activity, str , Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    private fun finishClassAfterSeeToast() {
        Toast.makeText(activity, "success" , Toast.LENGTH_SHORT).show()


        val handler = Handler()
        handler.postDelayed({
             activity.finish()

        }, 2000)
    }


    override fun onResume() {
        super.onResume()
        //keyboard
        Helper.keyboardHidden( activity )
    }
}