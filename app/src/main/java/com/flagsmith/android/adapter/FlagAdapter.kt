package com.flagsmith.android.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.response.Flag
import com.flagmsith.android.R


interface FlagPickerSelect {
    fun click(favContact: Flag?)
}


class FlagAdapter(
    private val context: Context,
    private val resultList: List<Flag>,
    private var contactPickerSelect: FlagPickerSelect
) :
    RecyclerView.Adapter<FlagAdapter.MyViewHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_flag, null, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val m = resultList[position]
        holder.tvFlagname.text = m.feature.name
        holder.tvFlagvalue.text = m.feature.initialValue
        holder.tvStatus.text = m.enabled.toString()
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
        var tvStatus: TextView
        var tvFlagname: TextView
        var tvFlagvalue : TextView

        //        TextView tv_contact_number;
        var layCardClickItemflag: CardView

        init {
            tvFlagvalue = itemView.findViewById(R.id.tv_flagValue)
            tvFlagname = itemView.findViewById(R.id.tv_flagName)
            tvStatus = itemView.findViewById(R.id.tv_status)
            layCardClickItemflag = itemView.findViewById(R.id.lay_card_click_itemFlag)
        }
    }
}