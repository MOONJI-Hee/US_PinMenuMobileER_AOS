package com.wooriyo.us.pinmenumobileer.printer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.databinding.ListCateCheckBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.CategoryDTO

class CateAdapter(val dataSet: ArrayList<CategoryDTO>): RecyclerView.Adapter<CateAdapter.ViewHolder>() {
    lateinit var itemClickListener: ItemClickListener

    fun setOnCheckClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListCateCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val binding: ListCateCheckBinding, val itemClickListener: ItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CategoryDTO) {
            binding.name.text = data.name
            binding.name.setOnCheckedChangeListener { v, isChecked ->
                itemClickListener.onCheckClick(adapterPosition, v as CheckBox, isChecked)
            }
        }
    }
}
