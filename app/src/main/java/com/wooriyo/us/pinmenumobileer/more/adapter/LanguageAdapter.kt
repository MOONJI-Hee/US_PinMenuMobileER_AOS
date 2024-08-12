package com.wooriyo.us.pinmenumobileer.more.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ListLanguageBinding
import com.wooriyo.us.pinmenumobileer.model.LangDTO

class LanguageAdapter(val dataSet: LangDTO): RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ListLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind()
    }

    override fun getItemCount(): Int {
        return 14
    }
    class ViewHolder(val context: Context, val binding: ListLanguageBinding): RecyclerView.ViewHolder(binding.root) {
        val arrName = context.resources.getStringArray(R.array.language)
        val arrImg = context.resources.getStringArray(R.array.language_img)
        fun bind() {
            binding.tvLang.text = arrName[adapterPosition]
            binding.imgLang.setImageResource(context.resources.getIdentifier(arrImg[adapterPosition], "drawable", "com.wooriyo.us.pinmenumobileer"))
        }
    }
}