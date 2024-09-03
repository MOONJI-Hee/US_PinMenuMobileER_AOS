package com.wooriyo.us.pinmenumobileer.fcm

import android.app.*
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rt.printerlibrary.cmd.Cmd
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.setting.TextSetting
import com.sewoo.jpos.command.ESCPOSConst
import com.wooriyo.us.pinmenumobileer.BaseActivity.Companion.currentActivity
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.escposPrinter
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.CHANNEL_ID_CALL
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.CHANNEL_ID_ORDER
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.NOTIFICATION_ID_CALL
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.NOTIFICATION_ID_ORDER
import com.wooriyo.us.pinmenumobileer.history.ByHistoryActivity
import com.wooriyo.us.pinmenumobileer.member.StartActivity
import com.wooriyo.us.pinmenumobileer.model.OrderHistoryDTO
import com.wooriyo.us.pinmenumobileer.model.ReceiptDTO
import com.wooriyo.us.pinmenumobileer.store.StoreListFragment
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import com.wooriyo.us.pinmenumobileer.util.PrinterHelper
import retrofit2.Call
import retrofit2.Response
import java.io.UnsupportedEncodingException


class MyFirebaseService: FirebaseMessagingService() {
    val TAG = "MyFirebase"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MyApplication.pref.setToken(token)
        Log.d(TAG, "new Token >> $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        println(message.data.toString())
        println(message.notification)

        createNotification(message)

        if(currentActivity != null) {
            Log.d(TAG, "currentActivity.localClassName >> ${currentActivity!!.localClassName}")

            if(currentActivity!!.localClassName == "history.ByHistoryActivity") {
                if(message.data["chk_udt"] == "1") {
                    if(message.data["moredata"] == "call") {
                        (currentActivity as ByHistoryActivity).newCall()
                    }else if (message.data["reserv_type"] == "0") {
                        (currentActivity as ByHistoryActivity).newOrder()
                    } else {
                        (currentActivity as ByHistoryActivity).newReservation()
                    }
                }
            }else if (currentActivity!!.localClassName == "MainActivity") {
                val currentFragment = (currentActivity as MainActivity).supportFragmentManager.findFragmentById(R.id.fragment)
                if(currentFragment?.id == StoreListFragment.newInstance().id) {
                    (currentFragment as StoreListFragment).getStoreList()
                }
            }
        }

        if(message.data["moredata"] == "call") {
            //TODO 호출 영수증 뽑기
        } else {
            val ordCode_key = message.data["moredata"]
            val ordCode = message.data["moredata_ordcode"]

            ApiClient.service.getReceipt(ordCode_key.toString()).enqueue(object : retrofit2.Callback<OrderHistoryDTO>{
                override fun onResponse(call: Call<OrderHistoryDTO>, response: Response<OrderHistoryDTO>) {
                    Log.d(TAG, "단건 주문 조회 URL : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return

                    when(result.status) {
                        1 -> {
                            result.ordcode = ordCode ?: ""
                            PrinterHelper.print(result, applicationContext)
                        }
                        else -> Toast.makeText(applicationContext, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderHistoryDTO>, t: Throwable) {
                    Toast.makeText(applicationContext, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "단건 주문 조회 오류 >> $t")
                    Log.d(TAG, "단건 주문 조회 오류 >> ${call.request()}")
                }
            })
        }
    }

    private fun createNotification(message: RemoteMessage) {
        val notificationManager = NotificationManagerCompat.from(this)

        var channelId = ""
        var notificationId = 0

        when(message.data["moredata"]) {
            "call" -> {
                channelId = CHANNEL_ID_CALL
                notificationId = NOTIFICATION_ID_CALL
            }
            else -> {
                channelId = CHANNEL_ID_ORDER
                notificationId = NOTIFICATION_ID_ORDER
            }
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createPendingIntent () : PendingIntent {
        val intent = Intent(this, StartActivity::class.java)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(StartActivity::class.java)
        stackBuilder.addNextIntent(intent)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        else
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
    }
}