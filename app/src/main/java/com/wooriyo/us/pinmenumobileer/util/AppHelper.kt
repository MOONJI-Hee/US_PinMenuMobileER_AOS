package com.wooriyo.us.pinmenumobileer.util

import android.app.Activity
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
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.appver
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    }
}