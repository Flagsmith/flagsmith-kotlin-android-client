package com.flagsmith.android.screens.trait

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.Flagsmith
import com.flagsmith.entities.Trait
import com.flagsmith.android.R
import com.flagsmith.android.adapter.TraitAdapter
import com.flagsmith.android.adapter.TraitPickerSelect
import com.flagsmith.android.helper.Helper
import com.flagsmith.android.toolbar.ToolbarSimple


class TraitsActivity : AppCompatActivity() {

    lateinit var flagsmith: Flagsmith

    lateinit var activity: Activity
    lateinit var context: Context

    lateinit var rvTraits: RecyclerView

    lateinit var prgTraits: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traits)

        context = this
        activity = this

        //find by ids
        rvTraits = findViewById(R.id.rv_traits)
        prgTraits = findViewById(R.id.prg_traits)

        initBuilder()

        setupToolbar()

        setupButtonCreate()
    }

    private fun setupButtonCreate() {
        val btCreateTrait: Button = findViewById(R.id.bt_create_trait)
        btCreateTrait.setOnClickListener {
            val i = Intent(this, TraitCreateActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun setupToolbar() {
        ToolbarSimple(this, R.id.toolbarTraitsList, "Traits")
    }

    private fun initBuilder() {
        flagsmith = Flagsmith(environmentKey = Helper.environmentDevelopmentKey, context = context)
    }

    private fun getAllData() {
        prgTraits.visibility = View.VISIBLE

        flagsmith.getTraits(Helper.identity) { result ->
            Helper.callViewInsideThread(activity) {
                prgTraits.visibility = View.GONE
                result.fold(
                    onSuccess = { traits ->
                        if (traits.isEmpty()) {
                            Toast.makeText(this@TraitsActivity, "No Data Found", Toast.LENGTH_SHORT)
                                .show()
                            return@callViewInsideThread
                        }

                        createAdapterFlag(traits)
                    },
                    onFailure = {
                        Toast.makeText(this@TraitsActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun createAdapterFlag(list: List<Trait>) {
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL
        rvTraits.layoutManager = manager
        val customAdapter = TraitAdapter(context, list, object : TraitPickerSelect {
            override fun click(mSelect: Trait?) {

            }
        })
        rvTraits.adapter = customAdapter
    }

    override fun onResume() {
        super.onResume()

        getAllData()
    }

}