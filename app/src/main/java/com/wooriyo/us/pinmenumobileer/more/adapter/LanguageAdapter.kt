package com.wooriyo.us.pinmenumobileer.more.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ListLanguageBinding
import com.wooriyo.us.pinmenumobileer.model.LangResultDTO
import com.wooriyo.us.pinmenumobileer.model.LanguageDTO

class LanguageAdapter(val dataSet: ArrayList<LanguageDTO>): RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ListLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(dataSet[p1])

        if(p1 == 0) p0.binding.check.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val context: Context, val binding: ListLanguageBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LanguageDTO) {
            binding.run {
                layout.setOnClickListener {
                    check.isChecked = !check.isChecked
                    data.isChecked = check.isChecked
                }

                check.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                        disable.visibility = View.GONE
                    else
                        disable.visibility = View.VISIBLE
                }

                tvLang.text = data.lang
                imgLang.setImageResource(context.resources.getIdentifier(data.img, "drawable", "com.wooriyo.us.pinmenumobileer"))
                check.isChecked = data.isChecked
            }
        }
    }
}