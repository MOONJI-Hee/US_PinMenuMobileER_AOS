package com.wooriyo.us.pinmenumobileer.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.databinding.ListMenuSeqBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemMoveListener
import com.wooriyo.us.pinmenumobileer.model.GoodsDTO
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class GoodsSeqAdapter(val dataSet: ArrayList<GoodsDTO>): RecyclerView.Adapter<GoodsSeqAdapter.ViewHolder>(),
    ItemMoveListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListMenuSeqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val data: GoodsDTO = dataSet[fromPosition]
        dataSet.removeAt(fromPosition)
        dataSet.add(toPosition, data)
        notifyItemMoved(fromPosition, toPosition)

//        Collections.swap(dataSet, fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolder(val binding: ListMenuSeqBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GoodsDTO) {
            binding.name.text = data.name
            binding.price.text = AppHelper.price(data.price)
        }
    }
}