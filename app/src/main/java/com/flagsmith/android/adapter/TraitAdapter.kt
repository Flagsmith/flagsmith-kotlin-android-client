package com.flagsmith.android.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.entities.Trait
import com.flagsmith.android.R


interface TraitPickerSelect {
    fun click( mSelect: Trait?)
}


class TraitAdapter(
    private val context: Context,
    private val resultList: List<Trait>,
    private var contactPickerSelect: TraitPickerSelect
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
        holder.tvFlagname.text = m.key
        holder.tvFlagvalue.text = m.value
        holder.layCardClickItemflag.setOnClickListener { contactPickerSelect.click(m) }
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
        var tvFlagname: TextView
        var tvFlagvalue : TextView
        var layCardClickItemflag: CardView
        init {
            tvFlagname = itemView.findViewById(R.id.tv_TraitName)
            tvFlagvalue = itemView.findViewById(R.id.tv_TraitValue)

            layCardClickItemflag = itemView.findViewById(R.id.lay_card_click_itemTrait)
        }
    }
}