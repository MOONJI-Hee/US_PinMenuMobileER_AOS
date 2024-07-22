package com.wooriyo.us.pinmenumobileer.history.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.dialog.AlertDialog
import com.wooriyo.us.pinmenumobileer.databinding.ListReservationBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.OrderHistoryDTO
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class ReservationAdapter(val dataSet: ArrayList<OrderHistoryDTO>): RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {
    lateinit var completeListener: ItemClickListener
    lateinit var deleteListener: ItemClickListener
    lateinit var printClickListener: ItemClickListener
    lateinit var confirmListener: ItemClickListener
    lateinit var setTableNoListener: ItemClickListener

    fun setOnCompleteListener(completeListener: ItemClickListener) {
        this.completeListener = completeListener
    }

    fun setOnDeleteListener(deleteListener: ItemClickListener) {
        this.deleteListener = deleteListener
    }

    fun setOnPrintClickListener(printClickListener: ItemClickListener) {
        this.printClickListener = printClickListener
    }

    fun setOnConfirmListener(confirmListener: ItemClickListener) {
        this.confirmListener = confirmListener
    }

    fun setOnTableNoListener(setTableNoListener: ItemClickListener) {
        this.setTableNoListener = setTableNoListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.rv.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.VERTICAL, false)
        return ViewHolder(binding, parent.context, completeListener, deleteListener, printClickListener, confirmListener, setTableNoListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].idx.toLong()
    }

    class ViewHolder(
        val binding: ListReservationBinding,
        val context: Context,
        val completeListener: ItemClickListener,
        val deleteListener: ItemClickListener,
        val printClickListener: ItemClickListener,
        val confirmListener: ItemClickListener?,
        val setTableNoListener: ItemClickListener?
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind (data : OrderHistoryDTO) {
            binding.run {
                rv.adapter = OrderDetailAdapter(data.olist)

                tableNo.text = data.tableNo
                regdt.text = data.regdt
                orderNo.text = data.ordcode

                if(data.tipPer == -1) {
                    tipProp.visibility = View.GONE
                    tvPer.visibility = View.GONE
                }else {
                    tipProp.visibility = View.VISIBLE
                    tvPer.visibility = View.VISIBLE
                    tipProp.text = data.tipPer.toString()
                }
                tipPrice.text = AppHelper.price(data.tip)
                taxPrice.text = AppHelper.price(data.tax)
                subPrice.text = AppHelper.price(data.amount)
                price.text = AppHelper.price(data.total_price)

                if(data.rlist.isNotEmpty()) {
                    val rsv = data.rlist[0]
                    reservTel.text = rsv.tel
                    reservName.text = rsv.name
                    reservRequest.text = rsv.memo
                    reservAddr.text = rsv.addr
                    reservDate.text = rsv.reserdt

                    if(rsv.memo.isEmpty()) {
                        tvRequest.visibility = View.GONE
                        reservRequest.visibility = View.GONE
                    }
                }

                when(data.reserType) {
                    1 -> {
                        clTableNo.visibility = View.VISIBLE
                        reservType.setBackgroundColor(Color.parseColor("#FF005E"))
                        ivReservType.setImageResource(R.drawable.icon_res_store)
                        tvDate.text = String.format(context.getString(R.string.reserv_date), "Store")
                    }
                    2 -> {
                        clTableNo.visibility = View.GONE
                        reservType.setBackgroundColor(Color.parseColor("#46B6FF"))
                        ivReservType.setImageResource(R.drawable.icon_res_togo)
                        tvDate.text = String.format(context.getString(R.string.reserv_date), "To-go")
                    }
                }

                if(data.iscompleted == 1) {
                    top.setBackgroundColor(Color.parseColor("#E0E0E0"))
                    clPrice.setBackgroundResource(R.drawable.bg_r6g)
                    btnComplete.setBackgroundResource(R.drawable.bg_r6g)
                    btnComplete.text = "Restore"
                    complete.visibility = View.VISIBLE
                    arrowTableNo.visibility = View.GONE
                    TextViewCompat.setTextAppearance(btnComplete, R.style.text15)
                } else if (data.isreser != 0) {
                    top.setBackgroundResource(R.color.main)
                    clPrice.setBackgroundResource(R.drawable.bg_r6g)
                    btnComplete.setBackgroundResource(R.drawable.bg_r6y)
                    btnComplete.text = "Done"
                    complete.visibility = View.GONE
                    arrowTableNo.visibility = View.VISIBLE
                    TextViewCompat.setTextAppearance(btnComplete, R.style.text18b)
                } else {
                    top.setBackgroundResource(R.color.main)
                    clPrice.setBackgroundResource(R.drawable.bg_r6y)
                    btnComplete.setBackgroundResource(R.drawable.bg_r6y)
                    binding.btnComplete.text = "Ok"
                    complete.visibility = View.GONE
                    arrowTableNo.visibility = View.VISIBLE
                    TextViewCompat.setTextAppearance(btnComplete, R.style.text18b)
                }

                delete.setOnClickListener { deleteListener.onItemClick(adapterPosition) }
                print.setOnClickListener {
                    if(MyApplication.bluetoothPort.isConnected) {
                        printClickListener.onItemClick(adapterPosition)
                    }else{
                        val fragmentActivity = context as FragmentActivity
                        AlertDialog("", context.getString(R.string.dialog_no_printer)).show(fragmentActivity.supportFragmentManager, "AlertDialog")
                    }
                }
                btnComplete.setOnClickListener {
                    if(data.isreser > 0)
                        completeListener.onItemClick(adapterPosition)
                    else
                        confirmListener?.onItemClick(adapterPosition)
                }
                clTableNo.setOnClickListener {
                    setTableNoListener?.onItemClick(adapterPosition)
                }
            }
        }
    }
}