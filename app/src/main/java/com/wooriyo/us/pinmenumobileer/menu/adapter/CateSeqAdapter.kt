package com.wooriyo.us.pinmenumobileer.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ListCateSetBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemMoveListener
import com.wooriyo.us.pinmenumobileer.model.CategoryDTO
import java.util.Collections

class CateSeqAdapter(val dataSet: ArrayList<CategoryDTO>): RecyclerView.Adapter<CateSeqAdapter.ViewHolder>(),
    ItemMoveListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListCateSetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.icon.setImageResource(R.drawable.btn_category_list_move)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
//        val data: CategoryDTO = dataSet[fromPosition]
//        dataSet.removeAt(fromPosition)
//        dataSet.add(toPosition, data)
//        notifyItemMoved(fromPosition, toPosition)

        Collections.swap(dataSet, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolder(val binding: ListCateSetBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CategoryDTO) {
            binding.name.text = data.name
            binding.sub.text = data.subname
        }
    }
}