package com.wooriyo.us.pinmenumobileer.menu.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ListOptAddBinding
import com.wooriyo.us.pinmenumobileer.databinding.ListOptEditBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.ValueDTO
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class OptValAdapter(val dataSet: ArrayList<ValueDTO>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var plusClickListener: View.OnClickListener
    lateinit var deleteClickListener: ItemClickListener

    fun setOnPlusClickListener(plusClickListener: View.OnClickListener) {
        this.plusClickListener = plusClickListener
    }

    fun setOnDeleteClickListener(deleteClickListener: ItemClickListener) {
        this.deleteClickListener = deleteClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListOptEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val bindingAdd = ListOptAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val arrayAdapter = ArrayAdapter.createFromResource(parent.context, R.array.opt_mark, R.layout.spinner_opt_mark)
        binding.mark.adapter = arrayAdapter

        return if(viewType == AppProperties.VIEW_TYPE_ADD) ViewHolderAdd(bindingAdd, parent.context, plusClickListener)
        else ViewHolder(binding, parent.context, deleteClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            AppProperties.VIEW_TYPE_ADD -> {
                holder as ViewHolderAdd
                holder.bind()
            }
            else -> {
                holder as ViewHolder
                holder.bind(dataSet[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == dataSet.size) AppProperties.VIEW_TYPE_ADD else position
    }

    override fun getItemCount(): Int {
        return dataSet.size + 1
    }

    class ViewHolder(val binding: ListOptEditBinding, val context: Context, val deleteClickListener: ItemClickListener): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ValueDTO) {
            binding.num.text = String.format(context.getString(R.string.opt_num), adapterPosition+1)

            if(data.name.isNotEmpty())
                binding.value.setText(data.name)
            else
                binding.value.text.clear()

            if(data.price.isNotEmpty() && data.price != "0") {
                binding.price.setText(AppHelper.price(data.price.toInt()))
            }else {
                binding.price.text.clear()
            }

            if (data.mark == "-")
                binding.mark.setSelection(1)
            else
                binding.mark.setSelection(0)

            binding.delete.setOnClickListener {
                deleteClickListener.onItemClick(adapterPosition)
            }

            binding.value.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    data.name = s.toString()
                }
            })

            binding.price.addTextChangedListener(object : TextWatcher{
                var result = ""
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!s.isNullOrEmpty() && s.toString() != result) {
                        result = AppHelper.price(s.toString().replace(",", "").toInt())
                        binding.price.setText(result)
                        binding.price.setSelection(result.length)
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if(s != null) {
                        data.price = (s.toString()).replace(",", "")
                    }
                }
            })

            binding.mark.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    data.mark = p0?.selectedItem.toString()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    class ViewHolderAdd(val binding: ListOptAddBinding, val context: Context, val plusClickListener: View.OnClickListener): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.num.text = String.format(context.getString(R.string.opt_num), adapterPosition+1)

            binding.btnPlus.setOnClickListener {
                plusClickListener.onClick(it)
            }
        }
    }
}