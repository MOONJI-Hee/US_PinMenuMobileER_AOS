package com.wooriyo.us.pinmenumobileer.menu

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.store
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ActivityAddGoodsBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.menu.adapter.OptAdapter
import com.wooriyo.us.pinmenumobileer.menu.dialog.DeleteDialog
import com.wooriyo.us.pinmenumobileer.model.GoodsDTO
import com.wooriyo.us.pinmenumobileer.model.OptionDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddGoodsActivity : BaseActivity() {
    lateinit var binding: ActivityAddGoodsBinding

    private val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    val radius = (6 * MyApplication.density).toInt()

    val optList = ArrayList<OptionDTO>()
    val optAdapter = OptAdapter(optList)

    var cate: String = ""
    var type: Int = 1   // 1: 추가, 2: 수정
    var goods: GoodsDTO?= null

    var file1: File ?= null
    var file2: File ?= null
    var file3: File ?= null

    var media1: MultipartBody.Part? = null
    var media2: MultipartBody.Part? = null
    var media3: MultipartBody.Part? = null

    var delImg1 = 0
    var delImg2 = 0
    var delImg3 = 0

    var selThum = 0

    private val pickImg = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if(it != null) { setImage(it) }
    }

    private val pickImgLgc = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK) {
            val imgUri = it.data?.data
            if(imgUri != null) setImage(imgUri)
        }
    }

    private val setOption = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when(it.resultCode) {
            Activity.RESULT_OK -> {
                val resultOpt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getSerializableExtra("result_opt", OptionDTO::class.java) ?: return@registerForActivityResult
                } else {
                    it.data?.getSerializableExtra("result_opt") ?: return@registerForActivityResult
                }
                optList.add(resultOpt as OptionDTO)
//                Log.d(TAG, "추가한 option > $resultOpt")
                optAdapter.notifyItemInserted(optList.size - 1)
            }
            AppProperties.RESULT_MODIFY -> {
                val resultOpt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getSerializableExtra("result_opt", OptionDTO::class.java) ?: return@registerForActivityResult
                } else {
                    it.data?.getSerializableExtra("result_opt") ?: return@registerForActivityResult
                }

                val position = it.data?.getIntExtra("position", -1) ?: -1

                if(position >= 0) {
                    optList[position] = resultOpt as OptionDTO
                    optAdapter.notifyItemChanged(position)
                }
            }
            AppProperties.RESULT_DELETE -> {
                val position = it.data?.getIntExtra("position", -1) ?: -1
                if(position >= 0) {
                    optList.removeAt(position)
                    optAdapter.notifyItemRemoved(position)
                }
            }
        }
    }

    private fun setImage (uri: Uri) {
        Log.d(TAG, "이미지 Uri >> $uri")

        var path = ""

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                path = cursor.getString(dataColumn)
                Log.d(TAG, "path >>>>> $path")
            }
        }

        when(selThum) {
            1 -> {
                Glide.with(mActivity)
                    .load(uri)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(binding.img1)
                binding.imgHint1.visibility = View.GONE
                binding.img1.visibility = View.VISIBLE
                binding.del1.visibility = View.VISIBLE

                file1 = File(path)
                delImg1 = 0
            }
            2 -> {
                Glide.with(mActivity)
                    .load(uri)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(binding.img2)
                binding.imgHint2.visibility = View.GONE
                binding.img2.visibility = View.VISIBLE
                binding.del2.visibility = View.VISIBLE

                file2 = File(path)
                delImg2 = 0
            }
            3 -> {
                Glide.with(mActivity)
                    .load(uri)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(binding.img3)
                binding.imgHint3.visibility = View.GONE
                binding.img3.visibility = View.VISIBLE
                binding.del3.visibility = View.VISIBLE

                file3 = File(path)
                delImg3 = 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            checkPermissions()
        }

        optAdapter.setOnItemClickListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(mActivity, AddOptActivity::class.java)
                intent.putExtra("type", 1)
                intent.putExtra("opt", optList[position].deepCopy())
                intent.putExtra("position", position)
                setOption.launch(intent)
            }
        })
        binding.rvOpt.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvOpt.adapter = optAdapter

        binding.status.adapter = ArrayAdapter(mActivity, R.layout.spinner_menu_status, R.id.item, resources.getStringArray(R.array.menu_icon))

        binding.price.addTextChangedListener(object: TextWatcher{
            var result = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.isNullOrEmpty() && s.toString() != result) {
                    result = AppHelper.price(s.toString().replace(",", "").toDouble())
                    binding.price.setText(result)
                    binding.price.setSelection(result.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.back.setOnClickListener { finish() }
        binding.save.setOnClickListener { getMenu() }

        type = intent.getIntExtra("type", type)

        if(type == 2) {
            binding.title.text = getText(R.string.menu_udt)
            binding.delete.visibility = View.VISIBLE

            goods = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getSerializableExtra("goods", GoodsDTO::class.java)
            else
                intent.getSerializableExtra("goods") as GoodsDTO

            setMenu(goods)

            binding.delete.setOnClickListener {
                DeleteDialog(2, goods!!.name) { delete() }.show(supportFragmentManager, "DeleteDialog")
            }
        }else {
            cate = intent.getStringExtra("cate") ?: ""
            goods = GoodsDTO(cate)
        }

        // 사진 선택 도구
        binding.thum1.setOnClickListener {checkUsePay(1)}
        binding.thum2.setOnClickListener {checkUsePay(2)}
        binding.thum3.setOnClickListener {checkUsePay(3)}

        // 사진 삭제
        binding.del1.setOnClickListener {
            binding.imgHint1.visibility = View.VISIBLE
            binding.img1.visibility = View.GONE
            binding.del1.visibility = View.GONE
            file1 = null
            delImg1 = 1
        }
        binding.del2.setOnClickListener {
            binding.imgHint2.visibility = View.VISIBLE
            binding.img2.visibility = View.GONE
            binding.del2.visibility = View.GONE
            file2 = null
            delImg2 = 1
        }
        binding.del3.setOnClickListener {
            binding.imgHint3.visibility = View.VISIBLE
            binding.img3.visibility = View.GONE
            binding.del3.visibility = View.GONE
            file3 = null
            delImg3 = 1
        }

        binding.optReq.setOnClickListener {
            val intent = Intent(mActivity, AddOptActivity::class.java)
            intent.putExtra("opt", OptionDTO(1))
            setOption.launch(intent)
        }
        binding.optChoice.setOnClickListener {
            val intent = Intent(mActivity, AddOptActivity::class.java)
            intent.putExtra("opt", OptionDTO(0))
            setOption.launch(intent)
        }
    }

    // 이미지 접근 권한 확인
    fun checkPermissions() {
        val deniedPms = ArrayList<String>()

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            for (pms in permission) {
                if(ActivityCompat.checkSelfPermission(mActivity, pms) != PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity, pms)) {
                        AlertDialog.Builder(mActivity)
                            .setTitle(R.string.pms_storage_title)
                            .setMessage(R.string.pms_storage_content)
                            .setPositiveButton(R.string.confirm) { dialog, _ ->
                                dialog.dismiss()
                                getStoragePms()
                            }
                            .setNegativeButton(R.string.cancel) {dialog, _ -> dialog.dismiss()}
                            .show()
                        return
                    }else {
                        deniedPms.add(pms)
                    }
                }
            }
        }

        if(deniedPms.isNotEmpty()) {
            getStoragePms()
        }
    }

    //권한 받아오기
    fun getStoragePms() {
        ActivityCompat.requestPermissions(mActivity, permission, AppProperties.REQUEST_STORAGE)
    }

    fun checkUsePay(position: Int) {
        if((store.payuse == "Y" && AppHelper.dateNowCompare(store.endDate))) {
            getImage(position)
        }else {
            Toast.makeText(mActivity, R.string.msg_no_pay, Toast.LENGTH_SHORT).show()
        }
    }

    fun getImage(position: Int) {
        selThum = position

        if(ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(mActivity)) {
            pickImg.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }else {
            pickImgLgc.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }
    }

    fun setMenu(gd: GoodsDTO?) {
        if(gd != null) {
            binding.run {
                name.setText(gd.name)
                explain.setText(gd.content)

                cookingTimeMin.setText(gd.cooking_time_min)
                cookingTimeMax.setText(gd.cooking_time_max)

                price.setText(AppHelper.price(gd.price))

                if(!gd.img1.isNullOrEmpty()) {
                    if(!gd.img1!!.contains("noimage.png")) {
                        Glide.with(mActivity)
                            .load(gd.img1)
                            .transform(CenterCrop(), RoundedCorners(radius))
                            .into(img1)
                        imgHint1.visibility = View.GONE
                        binding.img1.visibility = View.VISIBLE
                        binding.del1.visibility = View.VISIBLE
                    }
                }

                if(!gd.img2.isNullOrEmpty()) {
                    if(!gd.img2!!.contains("noimage.png")) {
                        Glide.with(mActivity)
                            .load(gd.img2)
                            .transform(CenterCrop(), RoundedCorners(radius))
                            .into(img2)
                        imgHint2.visibility = View.GONE
                        binding.img2.visibility = View.VISIBLE
                        binding.del2.visibility = View.VISIBLE
                    }
                }

                if(!gd.img3.isNullOrEmpty()) {
                    if(!gd.img3!!.contains("noimage.png")) {
                        Glide.with(mActivity)
                            .load(gd.img3)
                            .transform(CenterCrop(), RoundedCorners(radius))
                            .into(img3)
                        imgHint3.visibility = View.GONE
                        binding.img3.visibility = View.VISIBLE
                        binding.del3.visibility = View.VISIBLE
                    }
                }

                useSleep.isChecked = gd.adDisplay == "Y"
                useOpt.isChecked = gd.boption == "Y"

                status.setSelection(gd.icon - 1)
                Log.d(TAG, "gd.icon -1 >> ${gd.icon - 1}")
                Log.d(TAG, "Selected Item >> ${status.selectedItemPosition}")

                if(!gd.opt.isNullOrEmpty()) {
                    optList.addAll(gd.opt!!)
                    optAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun getMenu() {
        val gd = goods ?: GoodsDTO(cate)

        val jsonArray = JSONArray()

        binding.run {
            val strName = name.text.toString()

            if(strName.isEmpty()){
                Toast.makeText(mActivity, R.string.msg_no_goods_name, Toast.LENGTH_SHORT).show()
                return
            }

            loadingDialog.show(supportFragmentManager, "LoadingDialog")

            var strCookTimeMin = cookingTimeMin.text.toString()
            var strCookTimeMax = cookingTimeMax.text.toString()
            var strPrice = price.text.toString().replace(",", "")

            if(strCookTimeMin.isEmpty())
                strCookTimeMin = "0"

            if(strCookTimeMax.isEmpty())
                strCookTimeMax = "0"

            if(strPrice.isEmpty())
                strPrice = "0"

            gd.name = name.text.toString()
            gd.content = explain.text.toString()
            gd.cooking_time_min = strCookTimeMin
            gd.cooking_time_max = strCookTimeMax
            gd.price = strPrice.toDouble()

            gd.boption = if(useOpt.isChecked) "Y" else "N"
            gd.adDisplay = if(useSleep.isChecked) "Y" else "N"

            gd.icon = status.selectedItemPosition + 1

            gd.opt = optList

            if(!gd.opt.isNullOrEmpty()) {
                gd.opt!!.forEach {
                    val jsonObject = JSONObject()

                    jsonObject.put("optidx", it.idx)
                    jsonObject.put("optionTitle", it.title)

                    if(it.optreq == 0)
                        jsonObject.put("optionType", "선택")
                    else
                        jsonObject.put("optionType", "필수")

                    val jsonArray2 = JSONArray()
                    it.optval.forEach { value ->
                        val jsonObject2 = JSONObject()
                        jsonObject2.put("optionidx", value.idx)
                        jsonObject2.put("optionName", value.name)
                        jsonObject2.put("optionMark", value.mark)
                        jsonObject2.put("optionPrice", value.price)

                        jsonArray2.put(jsonObject2)
                    }
                    jsonObject.put("optionDetails", jsonArray2)

                    jsonArray.put(jsonObject)
                }
            }
        }

        when(type) {
            1 -> save(gd, jsonArray.toString())
            2 -> modify(gd, jsonArray.toString())
        }
    }

    // 신규 등록 모드 -> 수정 모드로 변경 (*메뉴 등록 성공했으나, 이미지 등록 실패했을 때 중복 등록 방지하기 위해 추가)
    fun changeMode() {
        if(type == 1) {
            type = 2
            Log.d(TAG, "goods.idx >> ${goods?.idx}")
            Log.d(TAG, "goods.name >> ${goods?.name}")
        }
    }

    fun save(gd: GoodsDTO, strJson : String) {
        Log.d(TAG, "strJson >>> $strJson")

        ApiClient.service.insGoods(useridx, storeidx, gd.category, gd.name, gd.content?:"", gd.cooking_time_min, gd.cooking_time_max, gd.price,
        gd.adDisplay, gd.icon, gd.boption, strJson).enqueue(object: Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "메뉴 등록 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return

                if(result.status == 1) {
                    gd.idx = result.idx
                    Log.d(TAG, "result.idx >> ${result.idx}")
                    Log.d(TAG, "gd.idx >> ${gd.idx}")
                    uploadImage(result.idx)
                }else {
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "메뉴 등록 실패> ${call.request()}")
                Log.d(TAG, "메뉴 등록 실패 > $t")
            }
        })
    }

    fun modify(gd: GoodsDTO, strJson : String) {
        Log.d(TAG, "strJson >>> $strJson")

        ApiClient.service.udtGoods(useridx, storeidx, gd.idx, gd.category, gd.name, gd.content?:"", gd.cooking_time_min, gd.cooking_time_max, gd.price,
         gd.icon, delImg1, delImg2, delImg3, gd.adDisplay, gd.boption, strJson).enqueue(object: Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "메뉴 수정 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return

                if(result.status == 1) {
                    uploadImage(gd.idx)
                }else {
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "메뉴 수정 실패> ${call.request()}")
                Log.d(TAG, "메뉴 수정 실패 > $t")
            }
        })
    }

    fun delete() {
        ApiClient.service.delGoods(useridx, storeidx, goods!!.idx).enqueue(object: Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "메뉴 삭제 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return

                if(result.status == 1) {
                    Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                    finish()
                }else {
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "메뉴 삭제 실패> ${call.request()}")
                Log.d(TAG, "메뉴 삭제 실패 > $t")
            }
        })

    }

    fun uploadImage(gidx: Int) {
        val mmtp = MediaType.parse("image/*")

        if(file1 != null) {
            val body = RequestBody.create(mmtp, file1!!)
            media1 = MultipartBody.Part.createFormData("img1", file1!!.name, body)
        }
        if(file2 != null) {
            val body = RequestBody.create(mmtp, file2!!)
            media2 = MultipartBody.Part.createFormData("img2", file2!!.name, body)
        }
        if(file3 != null) {
            val body = RequestBody.create(mmtp, file3!!)
            media3 = MultipartBody.Part.createFormData("img3", file3!!.name, body)
        }

        ApiClient.imgService.uploadImg(useridx, gidx, media1, media2, media3)
            .enqueue(object : Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "이미지 등록 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body()
                    if(result != null) {
                        when(result.status){
                            1 -> {
                                Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            else -> {
                                changeMode()
                                Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    loadingDialog.dismiss()
                }
                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "이미지 등록 실패 > $t")
                    Log.d(TAG, "이미지 등록 실패 > ${call.request()}")

                    changeMode()
                    loadingDialog.dismiss()
                }
            })
    }
}