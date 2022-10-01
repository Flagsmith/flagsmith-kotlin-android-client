package com.flagsmith.android.android.toolbar

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.flagmsith.R

class ToolbarSimple {


    var activity: Activity;
    var include : View;
    var title :String;

    constructor( activity: Activity , resIdInclude : Int , title :String  ) {
        this.activity = activity;
        this.title = title;

        include = activity.findViewById( resIdInclude );

        setTitle();
    }

    private fun setTitle() {
        var textView : TextView = include.findViewById(R.id.tv_title_toolbarInclude)
        textView.text = title;
    }
}