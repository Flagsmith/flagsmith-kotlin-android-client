package com.flagsmith.android.android.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.response.Trait
import com.flagmsith.R


interface TraitPickerSelect {
    fun click( mSelect: Trait?)
}


class TraitAdapter(
    private val context: Context,
    private val resultList: ArrayList<Trait>,
    var contactPickerSelect: TraitPickerSelect
) :
    RecyclerView.Adapter<TraitAdapter.MyViewHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_trait, null, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val m = resultList[position]
        holder.tv_flagName.text = m.trait_key
        holder.tv_flagValue.text = m.trait_value

        //click
        holder.lay_card_click_itemFlag.setOnClickListener { contactPickerSelect.click(m) }
    }

    override fun getItemCount(): Int {
        return resultList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tv_flagName: TextView
        var tv_flagValue : TextView

        var lay_card_click_itemFlag: CardView

        init {
            tv_flagName = itemView.findViewById(R.id.tv_TraitName)
            tv_flagValue = itemView.findViewById(R.id.tv_TraitValue)

            lay_card_click_itemFlag = itemView.findViewById(R.id.lay_card_click_itemTrait)
        }
    }
}