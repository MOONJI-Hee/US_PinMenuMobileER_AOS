package com.wooriyo.us.pinmenumobileer.pg.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ListPgBinding
import com.wooriyo.us.pinmenumobileer.model.PgDetailDTO
import com.wooriyo.us.pinmenumobileer.pg.PgCancelActivity
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class PgAdapter(val dataSet: ArrayList<PgDetailDTO>): RecyclerView.Adapter<PgAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListPgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val context: Context, val binding: ListPgBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PgDetailDTO) {
            binding.run {
                cardInfo.text = "${data.cardname} 뒷자리 ${data.cardnum}"
                regdt.text = data.regdt
                goods.text = data.name
                price.text = AppHelper.price(data.price)

                if(data.cancel == 0) {
                    cancelComplete.visibility = View.GONE
                    tvCancelComplete.visibility = View.GONE
                    btnCancel.visibility = View.VISIBLE
                }else {
                    cancelComplete.visibility = View.VISIBLE
                    tvCancelComplete.visibility = View.VISIBLE
                    btnCancel.visibility = View.GONE
                }

                btnCancel.setOnClickListener {
                    val intent = Intent(context, PgCancelActivity::class.java)
                    intent.putExtra("ordcode", data.ordcode_key)
                    context.startActivity(intent)
                }
            }
        }
    }
}