package com.wooriyo.us.pinmenumobileer.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.common.adapter.StoreAdapter
import com.wooriyo.us.pinmenumobileer.databinding.FragmentSelectStoreBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.menu.SetCategoryActivity
import com.wooriyo.us.pinmenumobileer.menu.adapter.MenuStoreAdapter

class SelectStoreFragment : Fragment() {
    lateinit var binding: FragmentSelectStoreBinding

    private var type: String? = null

    private val arg_type = "type"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(arg_type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectStoreBinding.inflate(layoutInflater)

        binding.rvStore.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val storeAdapter = StoreAdapter(MyApplication.storeList)

        storeAdapter.setOnItemClickListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                when (type) {
                    "menu"  ->  {
                        MyApplication.store = MyApplication.storeList[position]
                        MyApplication.storeidx = MyApplication.storeList[position].idx

                        startActivity(Intent(context, SetCategoryActivity::class.java))
                    }
                    "pay"   ->  (activity as MainActivity).insPaySetting(position)
                    "print" ->  (activity as MainActivity).insPrintSetting(position)
                    "qr"    ->  (activity as MainActivity).goQr(position)
                }
            }
        })
        binding.rvStore.adapter = storeAdapter

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            SelectStoreFragment().apply {
                arguments = Bundle().apply {
                    putString(arg_type, type)
                }
            }
    }
}