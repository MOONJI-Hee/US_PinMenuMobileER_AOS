package com.wooriyo.us.pinmenumobileer.menu.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ListOptBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.OptionDTO

class OptAdapter(val dataSet: ArrayList<OptionDTO>): RecyclerView.Adapter<OptAdapter.ViewHolder>() {
    lateinit var itemClickListener: ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListOptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    class ViewHolder(val binding: ListOptBinding, val context: Context, val itemClickListener: ItemClickListener): RecyclerView.ViewHolder(binding.root) {
        val strOpt = context.getString(R.string.choice)
        val strReq = context.getString(R.string.essential)
        val colOpt = Color.parseColor("#FFA701")
        val colReq = Color.parseColor("#FF0000")
        fun bind(data: OptionDTO) {
            binding.name.text = data.title

            when(data.optreq) {
                0 -> {
                    binding.req.text = strOpt
                    binding.req.setTextColor(colOpt)
                }
                1 -> {
                    binding.req.text = strReq
                    binding.req.setTextColor(colReq)
                }
            }

            binding.layout.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }
    }
}