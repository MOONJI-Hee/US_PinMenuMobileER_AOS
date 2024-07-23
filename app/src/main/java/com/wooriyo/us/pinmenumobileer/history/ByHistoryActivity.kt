package com.wooriyo.us.pinmenumobileer.history

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.rt.printerlibrary.cmd.Cmd
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.setting.TextSetting
import com.sewoo.jpos.command.ESCPOSConst
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.rtPrinter
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.dialog.ClearDialog
import com.wooriyo.us.pinmenumobileer.common.dialog.ConfirmDialog
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ActivityOrderListBinding
import com.wooriyo.us.pinmenumobileer.history.adapter.CallListAdapter
import com.wooriyo.us.pinmenumobileer.history.adapter.HistoryAdapter
import com.wooriyo.us.pinmenumobileer.history.adapter.OrderAdapter
import com.wooriyo.us.pinmenumobileer.history.adapter.ReservationAdapter
import com.wooriyo.us.pinmenumobileer.history.dialog.SetTableNoDialog
import com.wooriyo.us.pinmenumobileer.listener.DialogListener
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.CallHistoryDTO
import com.wooriyo.us.pinmenumobileer.model.CallListDTO
import com.wooriyo.us.pinmenumobileer.model.OrderHistoryDTO
import com.wooriyo.us.pinmenumobileer.model.OrderListDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.UnsupportedEncodingException

class ByHistoryActivity: BaseActivity() {
    lateinit var binding: ActivityOrderListBinding

    private val orderList = ArrayList<OrderHistoryDTO>()
    val orderAdapter = OrderAdapter(orderList)

    private val reservList = ArrayList<OrderHistoryDTO>()
    val reservAdapter = ReservationAdapter(reservList)

    private val callList = ArrayList<CallHistoryDTO>()
    val callAdapter = CallListAdapter(callList)

    private val completeList = ArrayList<OrderHistoryDTO>()
    val completeAdapter = HistoryAdapter(completeList)

    var selText: TextView?= null

    // 프린트 관련 변수
    var hyphen = StringBuilder()                // 하이픈
    var hyphen_num = AppProperties.HYPHEN_NUM   // 하이픈 개수
    var font_size = AppProperties.FONT_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        for (i in 1..hyphen_num) {
            hyphen.append("-")
        }

        selectTab(binding.tvOrder)

        setAdapterListener(completeAdapter, completeList)
        setOrderAdapter()
        setReservAdapter()
        setCallAdapter()

        binding.rv.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rv.adapter = orderAdapter

        binding.tabOrder.setOnClickListener {
            selectTab(binding.tvOrder)
            binding.rv.adapter = orderAdapter
            getOrderList()
            binding.newOrder.visibility = View.INVISIBLE
        }

        binding.tabReserv.setOnClickListener {
            selectTab(binding.tvReserv)
            binding.rv.adapter = reservAdapter
            getReservList()
            binding.newReserv.visibility = View.INVISIBLE
        }

        binding.tabCall.setOnClickListener {
            selectTab(binding.tvCall)
            binding.rv.adapter = callAdapter
            getCallList()
            binding.newCall.visibility = View.INVISIBLE
        }

        binding.tabCmplt.setOnClickListener {
            selectTab(binding.tvCmplt)
            binding.rv.adapter = completeAdapter
            getCompletedList()
        }

        binding.btnClear.setOnClickListener {
            ClearDialog({ clearCall() }, { clearOrder() }).show(supportFragmentManager, "ClearDialog")
        }

        binding.back.setOnClickListener { AppHelper.leaveStore(mActivity) }

//        templetDemo = TempletDemo.getInstance(rtPrinter, this)
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    fun selectTab(tv: TextView) {
        if(selText != tv) {
            selText?.setTextColor(Color.WHITE)
            selText?.setTypeface(null, Typeface.NORMAL)
            tv.setTextColor(ContextCompat.getColor(mActivity, R.color.main))
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            selText = tv
        }
    }

    fun reload() {
        when(selText) {
            binding.tvOrder -> getOrderList()
            binding.tvReserv -> getReservList()
            binding.tvCall -> getCallList()
            binding.tvCmplt -> getCompletedList()
        }
    }

    fun newOrder() {
        runOnUiThread{
            if(selText == binding.tvOrder) {
                getOrderList()
            }else {
                binding.newOrder.visibility = View.VISIBLE
            }
        }
    }

    fun newReservation() {
        runOnUiThread{
            if(selText == binding.tvReserv) {
                getReservList()
            }else {
                binding.newReserv.visibility = View.VISIBLE
            }
        }
    }

    fun newCall() {
        runOnUiThread {
            if(selText == binding.tvCall) {
                getCallList()
            }else {
                binding.newCall.visibility = View.VISIBLE
            }
        }
    }

    private fun setAdapterListener(adapter: HistoryAdapter, list: ArrayList<OrderHistoryDTO>) {
        adapter.setOnOrderCompleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)

                if(list[position].iscompleted == 0) {
                    showCompleteDialog("order") { completeOrder(list[position].idx, 1) }
                }else {
                    completeOrder(list[position].idx, 0)
                }
            }
        })

        adapter.setOnConfirmListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                confirmReservation(position, list)
            }
        })

        adapter.setOnDeleteListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {
                ConfirmDialog(
                    getString(R.string.delete),
                    getString(R.string.dialog_delete),
                    getString(R.string.delete)
                ) { deleteOrder(list[position].idx) }.show(supportFragmentManager, "DeleteDialog")
            }
        })

        adapter.setOnPrintClickListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {printRT(list[position])}
        })

        adapter.setOnTableNoListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {
                SetTableNoDialog(
                    list[position].idx,
                    object : DialogListener {
                        override fun onTableNoSet(tableNo: String) {
                            list[position].tableNo = tableNo
                            adapter.notifyItemChanged(position)
                        }
                    }).show(supportFragmentManager, "SetTableNoDialog")
            }
        })

        adapter.setOnCallCompleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                showCompleteDialog("call") { completeCall(list[position].idx) }
            }
        })

        adapter.setOnCallDeleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                ConfirmDialog(
                    getString(R.string.delete),
                    getString(R.string.dialog_delete),
                    getString(R.string.delete)
                ) { deleteCall(list[position].idx) }.show(supportFragmentManager, "DeleteCallDialog")
            }
        })
    }

    fun setOrderAdapter() {
        orderAdapter.setOnCompleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)

                if(orderList[position].iscompleted == 0) {
                    showCompleteDialog("order") { completeOrder(orderList[position].idx, 1) }
                }else {
                    completeOrder(orderList[position].idx, 0)
                }
            }
        })

        orderAdapter.setOnDeleteListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {
                ConfirmDialog(
                    getString(R.string.delete),
                    getString(R.string.dialog_delete),
                    getString(R.string.delete)
                ) { deleteOrder(orderList[position].idx) }.show(supportFragmentManager, "DeleteDialog")
            }
        })

        orderAdapter.setOnPrintClickListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {printRT(orderList[position])}
        })
    }

    fun setReservAdapter() {
        reservAdapter.setOnCompleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)

                if(reservList[position].iscompleted == 0) {
                    showCompleteDialog("order") { completeOrder(reservList[position].idx, 1) }
                }else {
                    completeOrder(reservList[position].idx, 0)
                }
            }
        })

        reservAdapter.setOnConfirmListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                confirmReservation(position, reservList)
            }
        })

        reservAdapter.setOnDeleteListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {
                ConfirmDialog(
                    getString(R.string.delete),
                    getString(R.string.dialog_delete),
                    getString(R.string.delete)
                ) { deleteOrder(reservList[position].idx) }.show(supportFragmentManager, "DeleteDialog")
            }
        })

        reservAdapter.setOnPrintClickListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {printRT(reservList[position])}
        })

        reservAdapter.setOnTableNoListener(object: ItemClickListener {
            override fun onItemClick(position: Int) {
                SetTableNoDialog(
                    reservList[position].idx,
                    object : DialogListener{
                        override fun onTableNoSet(tableNo: String) {
                            reservList[position].tableNo = tableNo
                            reservAdapter.notifyItemChanged(position)
                        }
                    }).show(supportFragmentManager, "SetTableNoDialog")
            }
        })
    }

    fun setCallAdapter() {
        callAdapter.setOnItemClickListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                showCompleteDialog("call") { completeCall(callList[position].idx) }
            }
        })
        callAdapter.setOnDeleteListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                ConfirmDialog(
                    getString(R.string.delete),
                    getString(R.string.dialog_delete),
                    getString(R.string.delete)
                ) { deleteCall(callList[position].idx) }.show(supportFragmentManager, "DeleteCallDialog")
            }
        })
    }

    fun showCompleteDialog(type: String, event: View.OnClickListener) {
        val completeDialog = ConfirmDialog("", String.format(getString(R.string.dialog_complete), type), getString(R.string.btn_complete), event)
        completeDialog.show(supportFragmentManager, "CompleteDialog")
    }

    // 주문 목록 조회
    fun getOrderList() {
        loadingDialog.show(supportFragmentManager)


        if(useridx == 0) useridx = 3
        if(storeidx == 0) storeidx = 5

        ApiClient.service.getOrderList(useridx, storeidx).enqueue(object: Callback<OrderListDTO> {
            override fun onResponse(call: Call<OrderListDTO>, response: Response<OrderListDTO>) {
                Log.d(TAG, "주문 목록 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body()
                if(result != null) {
                    when(result.status){
                        1 -> {
                            orderList.clear()
                            orderList.addAll(result.orderlist)

                            if(orderList.isEmpty()) {
                                binding.empty.visibility = View.VISIBLE
                                binding.rv.visibility = View.GONE
                            }else {
                                binding.empty.visibility = View.GONE
                                binding.rv.visibility = View.VISIBLE
                                orderAdapter.notifyDataSetChanged()
                            }
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }
                loadingDialog.dismiss()
            }

            override fun onFailure(call: Call<OrderListDTO>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "주문 목록 조회 오류 > $t")
                Log.d(TAG, "주문 목록 조회 오류 > ${call.request()}")
            }
        })
    }

    // 예약 목록 조회
    fun getReservList() {
        loadingDialog.show(supportFragmentManager)
        ApiClient.service.getReservList(useridx, storeidx).enqueue(object: Callback<OrderListDTO> {
            override fun onResponse(call: Call<OrderListDTO>, response: Response<OrderListDTO>) {
                Log.d(TAG, "예약 목록 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body()
                if(result != null) {
                    when(result.status){
                        1 -> {
                            reservList.clear()
                            reservList.addAll(result.orderlist)

                            if(reservList.isEmpty()) {
                                binding.empty.visibility = View.VISIBLE
                                binding.rv.visibility = View.GONE
                            }else {
                                binding.empty.visibility = View.GONE
                                binding.rv.visibility = View.VISIBLE
                                reservAdapter.notifyDataSetChanged()
                            }
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }
                loadingDialog.dismiss()
            }

            override fun onFailure(call: Call<OrderListDTO>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "예약 목록 조회 오류 > $t")
                Log.d(TAG, "예약 목록 조회 오류 > ${call.request()}")
            }
        })
    }

    // 호출 리스트 (히스토리) 조회
    fun getCallList() {
        loadingDialog.show(supportFragmentManager)
        ApiClient.service.getCallHistory(useridx, storeidx).enqueue(object: Callback<CallListDTO> {
            override fun onResponse(call: Call<CallListDTO>, response: Response<CallListDTO>) {
                Log.d(TAG, "호출 목록 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return

                if(result.status == 1){
                    callList.clear()
                    callList.addAll(result.callList)

                    if(callList.isEmpty()) {
                        binding.empty.visibility = View.VISIBLE
                        binding.rv.visibility = View.GONE
                    }else {
                        binding.empty.visibility = View.GONE
                        binding.rv.visibility = View.VISIBLE
                        callAdapter.notifyDataSetChanged()
                    }
                } else
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()

                loadingDialog.dismiss()
            }
            override fun onFailure(call: Call<CallListDTO>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "호출 목록 조회 오류 > $t")
                Log.d(TAG, "호출 목록 조회 오류 > ${call.request()}")
            }
        })
    }

    // 완료 목록 조회
    fun getCompletedList() {
        loadingDialog.show(supportFragmentManager)
        ApiClient.service.getCompletedList(useridx, storeidx).enqueue(object :
            Callback<OrderListDTO> {
            override fun onResponse(call: Call<OrderListDTO>, response: Response<OrderListDTO>) {
                Log.d(TAG, "완료 목록 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                if(result.status == 1) {
                    completeList.clear()
                    completeList.addAll(result.orderlist)

                    if(completeList.isEmpty()) {
                        binding.empty.visibility = View.VISIBLE
                        binding.rv.visibility = View.GONE
                    }else {
                        binding.empty.visibility = View.GONE
                        binding.rv.visibility = View.VISIBLE
                        completeAdapter.notifyDataSetChanged()
                    }
                }else
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()

                loadingDialog.dismiss()
            }

            override fun onFailure(call: Call<OrderListDTO>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "완료 목록 조회 오류 > $t")
                Log.d(TAG, "완료 목록 조회 오류 > ${call.request()}")
            }
        })
    }

    // 주문 초기화
    fun clearOrder() {
        ApiClient.service.clearOrder(useridx, storeidx).enqueue(object:Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "주문 초기화 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                if(result.status == 1){
                    if(selText != binding.tvCall) {
                        reload()
                    }
                }
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "주문 초기화 실패 > $t")
                Log.d(TAG, "주문 초기화 실패 > ${call.request()}")
            }
        })
    }

    // 호출 초기화
    fun clearCall() {
        ApiClient.service.clearCall(useridx, storeidx).enqueue(object:Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "직원호출 초기화 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                if(result.status == 1){
                    if(selText != binding.tvOrder)
                        reload()
                }
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "직원호출 초기화 실패 > $t")
                Log.d(TAG, "직원호출 초기화 실패 > ${call.request()}")
            }
        })
    }

    // 주문 완료 처리
    fun completeOrder(idx: Int, isCompleted: Int) {
        val status = if(isCompleted == 1) "Y" else "N"
        ApiClient.service.udtComplete(storeidx, idx, status)
            .enqueue(object:Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "주문 완료 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return
                    when(result.status){
                        1 -> {
                            Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                            reload()
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "주문 완료 실패 > $t")
                    Log.d(TAG, "주문 완료 실패 > ${call.request()}")
                }
            })
    }

    // 호출 완료 처리
    fun completeCall(idx: Int) {
        ApiClient.service.completeCall(storeidx, idx, "Y").enqueue(object:Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "호출 완료 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status){
                    1 -> {
                        Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                        reload()
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "호출 완료 실패 > $t")
                Log.d(TAG, "호출 완료 실패 오류 > ${call.request()}")
            }
        })
    }

    // 주문 삭제
    fun deleteOrder(idx: Int) {
        ApiClient.service.deleteOrder(storeidx, idx).enqueue(object:Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "주문 삭제 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status){
                    1 -> {
                        Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                        reload()
//                        orderList.removeAt(position)
//                        orderAdapter.notifyItemRemoved(position)
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "주문 삭제 실패 > $t")
                Log.d(TAG, "주문 삭제 실패 > ${call.request()}")
            }
        })
    }

    // 호촐 삭제
    fun deleteCall(idx: Int) {
        ApiClient.service.deleteCall(storeidx, idx).enqueue(object:Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "호출 삭제 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status){
                    1 -> {
                        Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                        reload()
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "호출 삭제 실패 > $t")
                Log.d(TAG, "호출 삭제 실패 > ${call.request()}")
            }
        })
    }

    // 예약 확인 처리
    fun confirmReservation(position: Int, list: ArrayList<OrderHistoryDTO>) {
        ApiClient.service.confirmReservation(useridx, storeidx, list[position].idx)
            .enqueue(object:Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "예약 확인 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return
                    when(result.status){
                        1 -> {
                            Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                            list[position].isreser = 1
                            when(selText) {
                                binding.tvReserv -> reservAdapter.notifyItemChanged(position)
                                binding.tvCmplt -> completeAdapter.notifyItemChanged(position)
                            }
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "예약 확인 실패 > $t")
                    Log.d(TAG, "예약 확인 실패 > ${call.request()}")
                }
            })
    }

    // 주문 프린트
    fun print(order: OrderHistoryDTO) {
        val pOrderDt = order.regdt
        val pTableNo = order.tableNo
        val pOrderNo = order.ordcode

        if(MyApplication.bluetoothPort.isConnected){
            MyApplication.escposPrinter.printAndroidFont(
                MyApplication.store.name,
                AppProperties.FONT_WIDTH,
                AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
            MyApplication.escposPrinter.printAndroidFont("주문날짜 : $pOrderDt",
                AppProperties.FONT_WIDTH,
                AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
            MyApplication.escposPrinter.printAndroidFont("주문번호 : $pOrderNo",
                AppProperties.FONT_WIDTH,
                AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
            MyApplication.escposPrinter.printAndroidFont("테이블번호 : $pTableNo",
                AppProperties.FONT_WIDTH,
                AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
            MyApplication.escposPrinter.printAndroidFont(
                AppProperties.TITLE_MENU,
                AppProperties.FONT_WIDTH,
                AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
            MyApplication.escposPrinter.printAndroidFont(hyphen.toString(),
                AppProperties.FONT_WIDTH, font_size, ESCPOSConst.LK_ALIGNMENT_LEFT)

            order.olist.forEach {
                val pOrder = AppHelper.getPrint(it)
                MyApplication.escposPrinter.printAndroidFont(pOrder,
                    AppProperties.FONT_WIDTH, font_size, ESCPOSConst.LK_ALIGNMENT_LEFT)
            }

            if(order.reserType > 0 && order.rlist.isNotEmpty()) {
                val reserv = order.rlist[0]

                MyApplication.escposPrinter.lineFeed(2)

                MyApplication.escposPrinter.printAndroidFont("전화번호",
                    AppProperties.FONT_WIDTH,
                    20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont(reserv.tel,
                    AppProperties.FONT_WIDTH,
                    33, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont("예약자명",
                    AppProperties.FONT_WIDTH,
                    20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont(reserv.name,
                    AppProperties.FONT_WIDTH,
                    33, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont("요청사항",
                    AppProperties.FONT_WIDTH,
                    20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont(reserv.memo,
                    AppProperties.FONT_WIDTH,
                    33, ESCPOSConst.LK_ALIGNMENT_LEFT)

                var str = ""
                when(order.reserType) {
                    1 -> str = "매장"
                    2 -> str = "포장"
                }
                MyApplication.escposPrinter.printAndroidFont(
                    String.format(getString(R.string.reserv_date), str),
                    AppProperties.FONT_WIDTH,
                    20, ESCPOSConst.LK_ALIGNMENT_LEFT)

                MyApplication.escposPrinter.printAndroidFont(reserv.reserdt,
                    AppProperties.FONT_WIDTH,
                    33, ESCPOSConst.LK_ALIGNMENT_LEFT)
            }

            MyApplication.escposPrinter.lineFeed(4)
            MyApplication.escposPrinter.cutPaper()
        }
    }

    fun printRT(order: OrderHistoryDTO) {
        Log.d(TAG, "printRT 시작")
        val escFac : CmdFactory = EscFactory()
        val escCmd : Cmd = escFac.create()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = "UTF-8"

        Log.d(TAG, "printRT 1111")

        val commonSetting = CommonSetting()
        commonSetting.align = CommonEnum.ALIGN_LEFT
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting))

        Log.d(TAG, "printRT 2222")


        val textSetting = TextSetting()
        textSetting.escFontType = ESCFontTypeEnum.FONT_A_12x24

        Log.d(TAG, "printRT 3333")


        try {
            val preBlank = ""
            textSetting.align = CommonEnum.ALIGN_LEFT
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Order Date : ${order.regdt}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Order No   : ${order.ordcode}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Table No   : ${order.tableNo}\n"))
            escCmd.append(escCmd.lfcrCmd)

            textSetting.doubleWidth = SettingEnum.Enable
            escCmd.append(escCmd.getTextCmd(textSetting,  "Product       Qty  Amt "))
            escCmd.append(escCmd.lfcrCmd)

            textSetting.doubleHeight = SettingEnum.Disable
            textSetting.doubleWidth = SettingEnum.Disable
            escCmd.append(escCmd.getTextCmd(textSetting, "--------------------------------------------"))
            escCmd.append(escCmd.lfcrCmd)

            textSetting.doubleWidth = SettingEnum.Enable

            order.olist.forEach {
                val pOrder = AppHelper.getPrintRT(it)
                escCmd.append(escCmd.getTextCmd(textSetting, pOrder))
                escCmd.append(escCmd.lfcrCmd)
            }

            escCmd.append(escCmd.cmdCutNew)

            Log.d(TAG, "printRT 444")


            rtPrinter.writeMsgAsync(escCmd.appendCmds)

            Log.d(TAG, "printRT 5555")


        } catch (e : UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d(TAG, "Exception > $e")

        }
    }
}