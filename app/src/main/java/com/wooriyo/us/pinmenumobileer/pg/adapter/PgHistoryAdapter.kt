package com.wooriyo.us.pinmenumobileer.pg.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.databinding.ListPgHistoryBinding
import com.wooriyo.us.pinmenumobileer.model.PgHistoryDTO

class PgHistoryAdapter(val dataSet: ArrayList<PgHistoryDTO>): RecyclerView.Adapter<PgHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListPgHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.rvPg.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.VERTICAL, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val binding: ListPgHistoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PgHistoryDTO) {
            binding.title.text = data.title

            if(!data.pgDetailList.isNullOrEmpty()) {
                binding.rvPg.adapter = PgAdapter(data.pgDetailList!!)
            }
        }
    }
}