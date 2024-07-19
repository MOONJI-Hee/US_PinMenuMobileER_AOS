package com.wooriyo.us.pinmenumobileer.util

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.recyclerview.widget.RecyclerView
import com.sam4s.io.ethernet.SocketInfo
import com.sam4s.printer.Sam4sFinder
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.appver
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bluetoothAdapter
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.connDev_sewoo
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.remoteDevices
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.BT_PRINTER
import com.wooriyo.us.pinmenumobileer.model.OrderDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.printer.sam4s.EthernetConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

// 자주 쓰는 메소드 모음 - 문지희 (2023.05 갱신)
class AppHelper {
    val TAG = "AppHelper"
    companion object {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val datetimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val appCallFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
        private val emailReg = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$".toRegex()
        private val pwReg = "^(?=.*[a-zA-Z0-9]).{8,}$".toRegex()

        // 네비게이션바 숨기기
        fun hideInset(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
            }else {
                activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }
        }

        // 바깥 클릭했을 때 키보드 내리기
        fun hideKeyboard(context: Context, focusView: View?, ev: MotionEvent) {
            if (focusView != null) {
                val rect = Rect()
                focusView.getGlobalVisibleRect(rect)
                val x = ev.x.toInt()
                val y = ev.y.toInt()
                if (!rect.contains(x, y)) {
                    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                    focusView.clearFocus()
                }
            }
        }

        fun verifyEmail(email: String):Boolean {
            return email.matches(emailReg)
        }

        fun verifyPw(pw: String): Boolean {
            return pw.matches(pwReg)
        }

        val dec = DecimalFormat("00")
        val nformat = NumberFormat.getInstance()

        // 천 단위 콤마 찍기
        fun price(d: Double): String {
            val nFormat = DecimalFormat("0.00")
            return nFormat.format(d).toString()
//            return nformat.format(n)
        }

        // 한자리 수 n > 0n 형식으로 변환하기 + 빈 문자열 > 00으로 변환
        fun mkDouble(n: String): String {
            return if(n == "") {
                "00"
            } else {
                dec.format(n.toInt())
            }
        }

        // 코드에서 리사이클러뷰 높이 지정
        fun setRvHeight(rv: RecyclerView, size: Int, itemHeight: Int) {
            val hdp = size * itemHeight
            val hpx = (hdp * MyApplication.density).toInt()

            val params = rv.layoutParams
            params.height = hpx
            rv.layoutParams = params
        }

        fun setHeight(v: View, height: Int) {
            val hpx = (height * MyApplication.density).toInt()

            val params = v.layoutParams
            params.height = hpx
            v.layoutParams = params
        }

        // 이미지뷰 일부만 corner radius 주기
        fun getRoundedCornerLT(view: View, radius: Float) {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, (view!!.width + radius).toInt(), (view.height + radius).toInt(), radius)
                }
            }
            view.clipToOutline = true
        }

        // 정수 > 00 형태의 문자열로 리턴
        fun intToString(n: Int): String {
            return if(n in 1..9) {"0$n"} else n.toString()
        }

        // 현재 날짜와 비교
        fun dateNowCompare(dt: String?): Boolean {    // 과거 : false, 현재 혹은 미래 : true
            return if(dt.isNullOrEmpty()) {
                false
            }else {
                val strDt = dt.replace(" ", "T")

                val now = LocalDateTime.now()
                val day = LocalDateTime.parse(strDt)

                val cmp = day.compareTo(now)

                cmp >= 0
            }
        }

        // 오늘 날짜와 비교 - true: 오늘, false : 오늘 아님
        fun CompareToday(strDate: String?): Boolean {
            return if (strDate.isNullOrEmpty()) {
                false
            } else {
                val list = strDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                val year = list[0].toInt()
                val month = list[1].toInt()
                val day = list[2].toInt()

                val today = LocalDate.now()
                val date = LocalDate.of(year, month, day)
                today.isEqual(date)
            }
        }

        // 현재 일시 yyyy-mm-dd HH:mm:ss 형식으로 리턴
        fun getToday(): String {
            val now = LocalDateTime.now()
            return datetimeFormat.format(now).toString()
        }

        // APPCALL 거래번호 생성
        fun getAppCallNo(): String {
            return appCallFormatter.format(LocalDateTime.now())
        }

        // 버전 비교
        fun compareVer(curver: String): Boolean {
            val arr_cur = curver.split(".")
            val arr_app = appver.split(".")

            if(curver == appver) return true

            for(i : Int in arr_app.indices) {
                if(arr_app[i].toInt() > arr_cur[i].toInt()) {
                    return true
                }
            }
            return false
        }

        fun osVersion(): Int = Build.VERSION.SDK_INT    // 안드로이드 버전
        fun versionName(context: Context): String = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        fun getPhoneModel(): String = Build.MODEL       // 디바이스 모델명

        fun leaveStore(activity: Activity) {
            ApiClient.service.leaveStore(MyApplication.useridx, MyApplication.storeidx, MyApplication.androidId)
                .enqueue(object : Callback<ResultDTO> {
                    override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                        Log.d("AppHelper", "매장 나가기 url : $response")
                        if(!response.isSuccessful) return

                        val result = response.body() ?: return
                        if(result.status == 1){
                            MyApplication.setStoreDTO()
                            MyApplication.storeidx = 0
                            activity.finish()
                        }else
                            Toast.makeText(activity, result.msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                        Toast.makeText(activity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                        Log.d("AppHelper", "매장 나가기 오류 > $t")
                        Log.d("AppHelper", "매장 나가기 오류 > ${call.request()}")
                    }
                })
        }

        // 블루투스 & 세우전자 프린터기 연결 관련 메소드

        // 블루투스 기기 찾기
        fun searchDevice() {
            Log.d("AppHelper", "searchDevice 시작")
            MyApplication.bluetoothAdapter.startDiscovery()
        }

        // 블루투스 연결
        fun connDevice(position: Int): Int {
            var retVal: Int = -1

            Log.d("AppHelper", "블루투스 기기 커넥트")
            Log.d("AppHelper", "remote 기기 > $remoteDevices")

            if(remoteDevices.isNotEmpty()) {
                val connDvc = remoteDevices[position]
                Log.d("AppHelper", "connDvc >> $connDvc")

                try {
                    MyApplication.bluetoothPort.connect(connDvc)
                    retVal = Integer.valueOf(0)
                    connDev_sewoo = remoteDevices[position].address
                } catch (e: IOException) {
                    e.printStackTrace()
                    retVal = Integer.valueOf(-1)
                }
            }else {
                retVal = -2
            }
            return retVal
        }

        // 페어링 된 기기 찾기
        fun getPairedDevice() : Int {
            Log.d("AppHelper", "getPairedDevice 시작")
            remoteDevices.clear()

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            pairedDevices?.forEach { device ->
//            val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                if(MyApplication.bluetoothPort.isValidAddress(deviceHardwareAddress)) {
                    val deviceNum = device.bluetoothClass.majorDeviceClass

                    if(deviceNum == BT_PRINTER) {
                        remoteDevices.add(device)
                    }
                }
            }
            Log.d("AppHelper", "페어링된 기기 목록 >>$remoteDevices")

            return if(remoteDevices.isNotEmpty()) 1 else 0
        }

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - RTP325
        fun getPrint(ord: OrderDTO) : String {
            val oneLine = AppProperties.RT_ONE_LINE_BIG     // 23
            val productLine = AppProperties.RT_PRODUCT_BIG  // 12
            val qtyLine = AppProperties.RT_QTY_BIG          // 3
            val amtLine = AppProperties.RT_AMT_BIG          // 6

            val result: StringBuilder = StringBuilder()
            val underline1 = StringBuilder()
            val underline2 = StringBuilder()
            val underline3 = StringBuilder()

            val length = ord.name.length

            if(length <= 12) {
                result.append(ord.name)
            }
            if(ord.name.length <= 24){
                underline1.append(ord.name.substring(0, ord.name.length -1))
            }
            if(ord.name){
                underline2.append(ord.name.substring(0, ord.name.length -1))
            }
            if(){
                underline3.append(ord.name.substring(0, ord.name.length -1))
            }

            if (MyApplication.store.fontsize == 1) {
                space = if(ord.price >= 100000) 1
                else if(ord.price >= 10000) 2
                else if (ord.price >= 1000) 3
                else if (ord.price >= 100) 6
                else if (ord.price >= 10) 7
                else if (ord.price >= 0) 8
                else 1

                if(ord.gea < 10) {
                    space++
                }

            } else if (MyApplication.store.fontsize == 2) {
                if (ord.gea < 10) {
                    diff += 1
                    space += 2
                } else if (ord.gea < 100) {
                    space += 1
                }
            }

            for (i in 1..diff) {
                result.append(" ")
            }

            result.append(ord.gea.toString())

            for (i in 1..space) {
                result.append(" ")
            }

//            var togo = ""
//            when (ord.togotype) {
//                1 -> togo = "신규"
//                2 -> togo = "포장"
//            }
//            result.append(togo)

            result.append(price(ord.price))

            if (underline1.toString() != "")
                result.append("\n$underline1")

            if (underline2.toString() != "")
                result.append("\n$underline2")

            if(!ord.opt.isNullOrEmpty()) {
                ord.opt.forEach {
                    result.append("\n -$it")
                }
            }

            return result.toString()
        }

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - 세우전자
        fun getPrint(ord: OrderDTO) : String {
            var one_line = AppProperties.ONE_LINE_BIG
            var space = AppProperties.SPACE

            var total = 0.0
            var name = 0.0

            val result: StringBuilder = StringBuilder()
            val underline1 = StringBuilder()
            val underline2 = StringBuilder()

            ord.name.forEach {
                val charSize = getSewooCharSize(it)
//                Log.d("AppHelper", "charSize $it >> $charSize")

                if (total + 1 < one_line){
                    result.append(it)
                    name += charSize
                }
                else if (total + 1 < (one_line * 2))
                    underline1.append(it)
                else
                    underline2.append(it)

                if (it == ' ') {
                    total++
                } else
                    total += charSize
            }

            var diff = (one_line - name + 1.5).toInt()

//            Log.d("AppHelper", "total >> $total")
//            Log.d("AppHelper", "name >> $name")
//            Log.d("AppHelper", "diff >> $diff")

            if (MyApplication.store.fontsize == 1) {
                space = if(ord.price >= 100000) 1
                        else if(ord.price >= 10000) 2
                        else if (ord.price >= 1000) 3
                        else if (ord.price >= 100) 6
                        else if (ord.price >= 10) 7
                        else if (ord.price >= 0) 8
                        else 1

                if(ord.gea < 10) {
                    space++
                }

            } else if (MyApplication.store.fontsize == 2) {
                if (ord.gea < 10) {
                    diff += 1
                    space += 2
                } else if (ord.gea < 100) {
                    space += 1
                }
            }

            for (i in 1..diff) {
                result.append(" ")
            }

            result.append(ord.gea.toString())

            for (i in 1..space) {
                result.append(" ")
            }

//            var togo = ""
//            when (ord.togotype) {
//                1 -> togo = "신규"
//                2 -> togo = "포장"
//            }
//            result.append(togo)

            result.append(price(ord.price))

            if (underline1.toString() != "")
                result.append("\n$underline1")

            if (underline2.toString() != "")
                result.append("\n$underline2")

            if(!ord.opt.isNullOrEmpty()) {
                ord.opt.forEach {
                    result.append("\n -$it")
                }
            }

            return result.toString()
        }

        fun getSewooCharSize(c: Char): Double {
            if(Pattern.matches("^[ㄱ-ㅎ가-힣]*\$", c.toString())) {
                return AppProperties.HANGUL_SIZE
            }
            if(Pattern.matches("^[A-Z]*\$", c.toString())) {
                return AppProperties.ENG_SIZE_UPPER
            }
            if(Pattern.matches("^[a-z]*\$", c.toString())) {
                return AppProperties.ENG_SIZE_LOWER
            }
            if(Pattern.matches("^[0-9]*\$", c.toString())) {
                return AppProperties.NUM_SIZE
            }

            return 1.0
        }

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - SAM4S
//        fun getSam4sPrint(ord: OrderDTO) : String {
//            var hangul_size = AppProperties.HANGUL_SIZE_SAM4S
//            var one_line = AppProperties.ONE_LINE_SAM4S
//            var space = AppProperties.SPACE_SAM4S
//
//            var total = 0.0
//
//            val result: StringBuilder = StringBuilder()
//            val underline1 = StringBuilder()
//            val underline2 = StringBuilder()
//
//            ord.name.forEach {
//                if(total + hangul_size <= one_line)
//                    result.append(it)
//                else if(total + hangul_size <= (one_line * 2))
//                    underline1.append(it)
//                else
//                    underline2.append(it)
//
//                if(it == ' ') {
//                    total++
//                }else
//                    total += hangul_size
//            }
//
//            val mlength = result.toString().length
//            val mHangul = result.toString().replace(" ", "").length
//            val mSpace = mlength - mHangul
//            val mLine = mHangul * hangul_size + mSpace
//
//            val diff = one_line - mLine + 1
//
//            if (ord.gea >= 100) {
//                space = 0
//            }else if(ord.gea >= 10) {
//                space = 1
//            }
//
//            for(i in 1..diff) {
//                result.append(" ")
//            }
//            result.append(ord.gea.toString())
//
//            for (i in 1..space) {
//                result.append(" ")
//            }
//
//            var togo = ""
//            when(ord.togotype) {
//                1-> togo = "신규"
//                2-> togo = "포장"
//            }
//            result.append(togo)
//
//            if(underline1.toString() != "")
//                result.append("\n$underline1")
//
//            if(underline2.toString() != "")
//                result.append("\n$underline2")
//
//            return result.toString()
//        }

        // SAM4S 프린터기 관련 메소드
        val finder: Sam4sFinder = Sam4sFinder()

        var scheduler:ScheduledExecutorService ?= null
        var future: ScheduledFuture<*>? = null

        val cubePrinterList = ArrayList<SocketInfo>()

        // 같은 ip내 GCUBE 프린터 검색
        fun searchCube(context: Context) {
            if (future != null) {
                future!!.cancel(false)
                while (!future!!.isDone) {
                    try {
                        Thread.sleep(500)
                    } catch (e: java.lang.Exception) {
                        break
                    }
                }
                future = null
            }
            scheduler = Executors.newSingleThreadScheduledExecutor()
            scheduler.let {
                finder.startSearch(context, Sam4sFinder.DEVTYPE_ETHERNET)
                future = it?.scheduleWithFixedDelay(
                    Runnable {
                        val list = getCubeList()

                        if(list != null && list.size > 0) {
                            cubePrinterList.clear()
                            list.forEach { cube -> cubePrinterList.add(cube as SocketInfo) }
//                            connectCube(context, cubeList[0] as SocketInfo)
                        }
                    }, 0, 500, TimeUnit.MILLISECONDS )
            }
        }

        fun getCubeList() : ArrayList<*>? {
            val list = finder.devices

            if(list != null && list.size > 0) {
                Log.d("AppeHelper", "device 찾음")
                Log.d("AppeHelper", "프린터 왜 정보 안나와.. >>>> ${(list[0] as SocketInfo).address}")
                Log.d("AppeHelper", "프린터 왜 정보 안나와.. >>>> ${(list[0] as SocketInfo).port}")

                stopSearchCube()

                return list
            }else {
                return null
            }
        }

        fun stopSearchCube() {
            Log.d("AppHelper", "Stop Search 들어옴")
            if (future != null) {
                future!!.cancel(false)
                while (!future!!.isDone) {
                    try {
                        Thread.sleep(500)
                    } catch (e: java.lang.Exception) {
                        break
                    }
                }
                future = null
            }
            finder.stopSearch()
        }

        fun destroySearchCube() {
            if(future != null) {
                future!!.cancel(false)
                while(!future!!.isDone()) {
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        break
                    }
                }
                future = null
            }
            scheduler?.shutdown()
        }

        fun connectCube(context: Context, info: SocketInfo) {
            MyApplication.INSTANCE.getPrinterConnection()?.ClosePrinter()
            val connection = EthernetConnection(context)
//            connection.setSocketInfo(info)
            connection.setName(info.address)
            connection.setAdress(info.address)
            connection.setPort(info.port)
            connection.OpenPrinter()

            Log.d("AppHelper", "Printer Status >> ${connection.getPrinterStatus()}")
            Log.d("AppHelper", "Printer IsConnected >> ${connection.IsConnected()}")

            MyApplication.INSTANCE.setPrinterConnection(connection)
//            checkCubeConn()
        }

        // GCUBE 연결되었는지 확인, 상태 return
        fun checkCubeConn(context: Context): Int {
            val cube = MyApplication.INSTANCE.getPrinterConnection()
            val connection = EthernetConnection(context)

            connection.OpenPrinter()

            if (connection.IsConnected()) {
                Log.d("AppHelper", "연결됨 들어옴")
            }

            val status = connection.getPrinterStatus()?:"Null Error"
            val name = connection.getPrinterName()?:""
            Log.d("AppHelper", "Printer Status >> $status")
            Log.d("AppHelper", "Printer name >> $name")

            return if (status == "Printer Ready") 1 else 0
        }
    }
}