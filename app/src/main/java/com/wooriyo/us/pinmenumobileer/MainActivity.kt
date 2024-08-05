package com.wooriyo.us.pinmenumobileer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Printer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pairedDevices
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pref
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeList
import com.wooriyo.us.pinmenumobileer.common.SelectStoreFragment
import com.wooriyo.us.pinmenumobileer.common.adapter.BannerAdapter
import com.wooriyo.us.pinmenumobileer.common.dialog.WelcomeDialog
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ActivityMainBinding
import com.wooriyo.us.pinmenumobileer.menu.SetCategoryActivity
import com.wooriyo.us.pinmenumobileer.model.PopupListDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.more.MoreFragment
import com.wooriyo.us.pinmenumobileer.printer.PrinterMenuFragment
import com.wooriyo.us.pinmenumobileer.qr.QrAgreeActivity
import com.wooriyo.us.pinmenumobileer.qr.SetQrcodeFragment
import com.wooriyo.us.pinmenumobileer.store.StoreListFragment
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import com.wooriyo.us.pinmenumobileer.util.PrinterHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var thread: Thread

    private val turnOnBluetoothResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) { checkBluetooth() }
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionsBt = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    @RequiresApi(33)
    val pms_noti : String = Manifest.permission.POST_NOTIFICATIONS

    var isMain = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        thread = Thread(Runnable{
            if(AppHelper.getPairedDevice() == 1) {
                val connectedPrinter = pref.getConnectedPrinter() ?: return@Runnable

                pairedDevices.forEach{
                    if(it.address == connectedPrinter.getString("address")
                        && it.uuids.toString() == connectedPrinter.getString("uuids")) {
                        if(PrinterHelper.checkSewoo(it))
                            PrinterHelper.connSewoo(mActivity, it)
                        else
                            PrinterHelper.connRT(mActivity, it)
                    }
                }
            }
        })

        val type : Int = intent.getIntExtra("type", 0)

        if(type == 0) {
            goMain()
        }

        // 알림 권한 확인
        if(MyApplication.osver >= 33) {
            checkNotiPms()
        }
        // 위치 권한 확인
        checkPermissions()

        // 전면 팝업 조회
        getWelcomePopup()
        // 배너 리스트 조회
        getBannerList()

        binding.run {
            icMain.setOnClickListener { goMain() }
            icMenu.setOnClickListener { setNavi(it.id) }
            icQr.setOnClickListener { setNavi(it.id) }
            icPrint.setOnClickListener { setNavi(it.id) }
            icMore.setOnClickListener { setNavi(it.id) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(thread.isAlive)
            thread.interrupt()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.isEmpty()) return

        when(requestCode) {
            AppProperties.REQUEST_ENABLE_BT -> {
                checkBluetoothPermission()
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetooth()
                }
            }
            AppProperties.REQUEST_LOCATION -> {
                grantResults.forEach {
                    if(it == PackageManager.PERMISSION_DENIED) {
                        getLocationPms()
                        return
                    }
                }
                if (MyApplication.osver >= Build.VERSION_CODES.S)
                    checkBluetoothPermission()
                else
                    checkBluetooth()
            }
        }
    }


    // SDK 33 이상에서 알림 권한 확인
    @RequiresApi(33)
    fun checkNotiPms() {
        when {
            ActivityCompat.checkSelfPermission(mActivity, pms_noti) == PackageManager.PERMISSION_DENIED -> getNotiPms()

            ActivityCompat.shouldShowRequestPermissionRationale(mActivity, pms_noti) -> {
                AlertDialog.Builder(mActivity)
                    .setTitle(R.string.pms_notification_title)
                    .setMessage(R.string.pms_notification_content)
                    .setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        getNotiPms()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
                    .show()
            }
        }
    }

    @RequiresApi(33)
    fun getNotiPms() {
        ActivityCompat.requestPermissions(mActivity, arrayOf(pms_noti), AppProperties.REQUEST_NOTIFICATION)
    }

    // 블루투스 ON/OFF 확인
    fun checkBluetooth() {
        if(!MyApplication.bluetoothAdapter.isEnabled) {   // 블루투스 꺼져있음
            turnOnBluetooth()
        }else {
            if(!thread.isAlive) thread.start()
        }
    }

    //블루투스 ON
    private fun turnOnBluetooth() {
        turnOnBluetoothResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun replace(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
    }

    private fun goMain() {
        if(!isMain) {
            binding.run{
                bottomNav.setBackgroundResource(R.drawable.bg_main_tabbar)
                ivMain.setImageResource(R.drawable.ic_main_tabar_main_s)
                ivMenu.setImageResource(R.drawable.icon_menuset_n_white)
                ivQr.setImageResource(R.drawable.icon_qr_n_white)
                ivPrint.setImageResource(R.drawable.icon_print_n_white)
                ivMore.setImageResource(R.drawable.ic_main_tabar_more_n)
                tvMain.setTextColor(getColor(R.color.main))
                setTxtWhite(tvMenu)
                setTxtWhite(tvQr)
                setTxtWhite(tvPrint)
                setTxtWhite(tvMore)
                binding.banner.visibility = View.VISIBLE
            }
        }
        isMain = true
        replace(StoreListFragment.newInstance())
    }

    private fun goSelStore(type: String) {
        binding.banner.visibility = View.GONE
        replace(SelectStoreFragment.newInstance(type))
    }

    private fun goMenuSet() {
        binding.banner.visibility = View.GONE
        binding.ivMenu.setImageResource(R.drawable.icon_menuset_p)
    }

    fun goQr(position: Int) {
        MyApplication.store = storeList[position]
        MyApplication.storeidx = storeList[position].idx

        binding.banner.visibility = View.GONE
        binding.ivQr.setImageResource(R.drawable.icon_qr_p)
        replace(SetQrcodeFragment.newInstance())
    }

    private fun goPrint() {
        binding.banner.visibility = View.GONE
        binding.ivPrint.setImageResource(R.drawable.icon_print_p)
        replace(PrinterMenuFragment.newInstance())
    }

    private fun goMore() {
        binding.banner.visibility = View.GONE
        binding.ivMore.setImageResource(R.drawable.ic_main_tabar_more_s)
        replace(MoreFragment.newInstance())
    }

    private fun setNavi(id:Int) {
        if(storeList.size != 1 || id != R.id.icMenu) {
            if(isMain) {
                binding.run{
                    bottomNav.setBackgroundColor(getColor(R.color.white))
                    ivMain.setImageResource(R.drawable.ic_main_tabar_main_n_white)
                    setTxtBlack(tvMain)
                    setTxtBlack(tvMenu)
                    setTxtBlack(tvQr)
                    setTxtBlack(tvPrint)
                    setTxtBlack(tvMore)
                }
            }
            isMain = false

            binding.run {
                ivMenu.setImageResource(R.drawable.icon_menuset_n_black)
                ivQr.setImageResource(R.drawable.icon_qr_n_black)
                ivPrint.setImageResource(R.drawable.icon_print_n_black)
                ivMore.setImageResource(R.drawable.ic_main_tabar_more_n_black)
            }
        }

        when(id) {
            R.id.icMenu -> {
                when(storeList.size) {
                    0 -> Toast.makeText(mActivity, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> {
                        MyApplication.store = storeList[0]
                        MyApplication.storeidx = storeList[0].idx

                        startActivity(Intent(mActivity, SetCategoryActivity::class.java))
                    }
                    else -> {
                        binding.ivMenu.setImageResource(R.drawable.icon_menuset_p)
                        goSelStore("menu")
                    }
                }
            }

            R.id.icQr -> {
                when(storeList.size) {
                    0 -> Toast.makeText(mActivity, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> goQr(0)
                    else -> {
                        binding.ivQr.setImageResource(R.drawable.icon_qr_p)
                        goSelStore("qr")
                    }
                }
            }

            R.id.icPrint -> {
                when(storeList.size) {
                    0 -> Toast.makeText(mActivity, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> insPrintSetting(0)
                    else -> {
                        binding.ivPrint.setImageResource(R.drawable.icon_print_p)
                        goSelStore("print")
                    }
                }
            }

            R.id.icMore -> goMore()
        }
    }

    fun setTxtBlack(tv:TextView) {
        tv.setTextColor(getColor(R.color.black))
    }

    fun setTxtWhite(tv:TextView) {
        tv.setTextColor(getColor(R.color.white))
    }

    fun getWelcomePopup() {
        ApiClient.service.getWelcomePopup(0, 0, 0)?.enqueue(object : Callback<PopupListDTO?>{
            override fun onResponse(call: Call<PopupListDTO?>, response: Response<PopupListDTO?>) {
                Log.d(TAG, "전면 팝업 조회  url : $response")
                if(!response.isSuccessful) return
                val result = response.body() ?: return

                if(result.status != 1 || result.popupList.isNullOrEmpty()) return

                if(!AppHelper.CompareToday(pref.getNoPopupDate())) {
                    WelcomeDialog(result.popupList[0]).show(supportFragmentManager, "WelcomeDialog")
                }
            }

            override fun onFailure(call: Call<PopupListDTO?>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "전면 팝업 조회 실패 >> $t")
                Log.d(TAG, "전면 팝업 조회 실패 >> ${call.request()}")
            }
        })
    }

    fun getBannerList() {
        ApiClient.service.getBannerList(MyApplication.useridx, 0, 0, 0)?.enqueue(object : Callback<PopupListDTO?>{
            override fun onResponse(call: Call<PopupListDTO?>, response: Response<PopupListDTO?>) {
                Log.d(TAG, "배너 리스트 조회  url : $response")
                if(!response.isSuccessful) return
                val result = response.body() ?: return

                if(result.status != 1 || result.bannerList.isNullOrEmpty()) return

                val bannerAdapter = BannerAdapter(result.bannerList)
                binding.banner.adapter = bannerAdapter
            }

            override fun onFailure(call: Call<PopupListDTO?>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "배너 리스트 조회 실패 >> $t")
                Log.d(TAG, "배너 리스트 조회 실패 >> ${call.request()}")
            }
        })
    }

    fun insPaySetting(position: Int) {
        ApiClient.service.insPaySetting(
            MyApplication.useridx, storeList[position].idx,
            MyApplication.androidId
        )
            .enqueue(object : Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "결제 설정 최초 진입 시 row 추가 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return

                    if(result.status == 1) {
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        MyApplication.bidx = result.bidx
                    }else
                        Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "결제 설정 최초 진입 시 row 추가 오류 >> $t")
                    Log.d(TAG, "결제 설정 최초 진입 시 row 추가 오류 >> ${call.request()}")
                }
            })
    }

    fun insPrintSetting(position: Int) {
        ApiClient.service.insPrintSetting(
            MyApplication.useridx, storeList[position].idx,
            MyApplication.androidId
        )
            .enqueue(object : retrofit2.Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "프린터 설정 최초 진입 시 row 추가 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return

                    if(result.status == 1){
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        MyApplication.bidx = result.bidx
                        goPrint()
                    }else
                        Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "프린터 설정 최초 진입 시 row 추가 오류 >> $t")
                    Log.d(TAG, "프린터 설정 최초 진입 시 row 추가 오류 >> ${call.request()}")
                }
            })
    }

    fun checkDeviceLimit(position: Int) {
        ApiClient.service.checkDeviceLimit(
            MyApplication.useridx, storeList[position].idx, pref.getToken().toString(), MyApplication.androidId, 0)
            .enqueue(object : Callback<ResultDTO> {
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "이용자수 체크 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return
                    if(result.status == 1){
                        goMenuSet()
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx

                        startActivity(Intent(mActivity, SetCategoryActivity::class.java))
                    }else
                        Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "이용자수 체크 오류 > $t")
                    Log.d(TAG, "이용자수 체크 오류 > ${call.request()}")
                }
            })
    }

    // 이 밑부터 삭제 예정

    // 위치 권한 확인하기
    fun checkPermissions() {
        val deniedPms = ArrayList<String>()

        for (pms in permissions) {
            if(ActivityCompat.checkSelfPermission(mActivity, pms) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity, pms)) {
                    AlertDialog.Builder(mActivity)
                        .setTitle(R.string.pms_location_content)
                        .setMessage(R.string.pms_location_content)
                        .setPositiveButton(R.string.confirm) { dialog, _ ->
                            dialog.dismiss()
                            getLocationPms()
                        }
                        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
                        .show()
                    return
                }else {
                    deniedPms.add(pms)
                }
            }
        }

        if(deniedPms.isEmpty()) {
            if (MyApplication.osver >= Build.VERSION_CODES.S)
                checkBluetoothPermission()
            else
                checkBluetooth()
        }else {
            getLocationPms()
        }
    }

    //권한 받아오기
    fun getLocationPms() {
        ActivityCompat.requestPermissions(mActivity, permissions, AppProperties.REQUEST_LOCATION)
    }

    fun checkBluetoothPermission() {
        val deniedPms = ArrayList<String>()

        for (pms in permissionsBt) {
            if(ActivityCompat.checkSelfPermission(mActivity, pms) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity, pms)) {
                    AlertDialog.Builder(mActivity)
                        .setTitle(R.string.pms_bluetooth_title)
                        .setMessage(R.string.pms_bluetooth_content)
                        .setPositiveButton(R.string.confirm) { dialog, _ ->
                            getBluetoothPms()
                            dialog.dismiss()
                            return@setPositiveButton
                        }
                        .setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                            return@setNegativeButton
                        }
                        .show()
                    return
                }else
                    deniedPms.add(pms)
            }
        }

        if(deniedPms.isEmpty() || deniedPms.size == 0) {
            checkBluetooth()
        }else {
            getBluetoothPms()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getBluetoothPms() {
        ActivityCompat.requestPermissions(mActivity, permissionsBt, AppProperties.REQUEST_ENABLE_BT)
    }
}