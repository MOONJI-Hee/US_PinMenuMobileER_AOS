package com.wooriyo.us.pinmenumobileer.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pref
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeList
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.SelectStoreActivity
import com.wooriyo.us.pinmenumobileer.databinding.FragmentMoreBinding
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class MoreFragment : Fragment() {
    lateinit var binding: FragmentMoreBinding
    val TAG = "MoreFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMoreBinding.inflate(layoutInflater)

        binding.run {
            userid.text = pref.getMbrDTO()?.userid

            manual.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(MyApplication.manualPdf), "application/pdf")
                }
                startActivity(Intent.createChooser(browserIntent, "Choose an Application"))
            }

            menuUi.setOnClickListener{
                when(storeList.size) {
                    0 -> Toast.makeText(context, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> {
                        if((storeList[0].payuse == "Y" && AppHelper.dateNowCompare(storeList[0].endDate))) {
                            MyApplication.store = storeList[0]
                            MyApplication.storeidx = storeList[0].idx
                            startActivity(Intent(context, SetMenuUiActivity::class.java))
                        }else {
                            Toast.makeText(context, R.string.msg_no_pay, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else ->  {
                        startActivity(
                            Intent(requireContext(), SelectStoreActivity::class.java).apply{ putExtra("type", "viewmode") }
                        )
                    }
                }
            }
            storeImg.setOnClickListener {
                when(storeList.size) {
                    0 -> Toast.makeText(context, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> {
                        MyApplication.store = storeList[0]
                        MyApplication.storeidx = storeList[0].idx
                        startActivity(Intent(context, SetStoreImgActivity::class.java))
                    }
                    else ->  {
                        startActivity(
                            Intent(requireContext(), SelectStoreActivity::class.java).apply{ putExtra("type", "storeImg") }
                        )
                    }
                }
            }
            tiptax.setOnClickListener {
                when(storeList.size) {
                    0 -> Toast.makeText(context, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> {
                        MyApplication.store = storeList[0]
                        MyApplication.storeidx = storeList[0].idx
                        startActivity(Intent(context, TipTaxActivity::class.java))
                    }
                    else ->  {
                        startActivity(
                            Intent(requireContext(), SelectStoreActivity::class.java).apply{ putExtra("type", "tiptax") }
                        )
                    }
                }
            }
            language.setOnClickListener {
                when(storeList.size) {
                    0 -> Toast.makeText(context, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> (context as MainActivity).insLangSetting(0)
                    else -> (context as MainActivity).goSelStore("language")
                }
            }
            setting.setOnClickListener { requireContext().startActivity(Intent(requireContext(), SetActivity::class.java)) }

            timezone.setOnClickListener {
                when(storeList.size) {
                    0 -> Toast.makeText(context, R.string.msg_no_store, Toast.LENGTH_SHORT).show()
                    1 -> {
                        MyApplication.store = storeList[0]
                        MyApplication.storeidx = storeList[0].idx
                        startActivity(Intent(context, TimezoneActivity::class.java).apply{ putExtra("pre", "more")})
                    }
                    else ->  {
                        startActivity(
                            Intent(requireContext(), SelectStoreActivity::class.java).apply{ putExtra("type", "timezone") }
                        )
                    }
                }
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MoreFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}