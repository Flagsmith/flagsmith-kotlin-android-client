package com.flagsmith.android.toolbar

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.flagsmith.android.R

class ToolbarSimple(activity: Activity, resIdInclude: Int, var title: String) {
    var include: View

    init {
        include = activity.findViewById(resIdInclude)
        setTitle()
    }

    private fun setTitle() {
        val textView: TextView = include.findViewById(R.id.tv_title_toolbarInclude)
        textView.text = title
    }
}