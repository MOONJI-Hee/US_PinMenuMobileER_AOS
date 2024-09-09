package com.wooriyo.us.pinmenumobileer.printer

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivityDetailPrinterBinding
import java.util.UUID
import java.util.concurrent.Executor
import java.util.regex.Pattern

class DetailPrinterActivity : BaseActivity() {
    lateinit var binding: ActivityDetailPrinterBinding
    lateinit var printer : BluetoothDevice



    val SELECT_DEVICE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printer =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra("device", BluetoothDevice::class.java) ?: return
            else
                intent.getParcelableExtra("device") ?: return


        val img = intent.getIntExtra("img", 0)
        val model = intent.getStringExtra("model")

        binding.ivPrinter.setImageResource(img)
        binding.model.text = model

        binding.etNickPrinter.setText(printer.alias)



        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            // Match only Bluetooth devices whose name matches the pattern.
            .setNamePattern(Pattern.compile(printer.name))
            // Match only Bluetooth devices whose service UUID matches this pattern.
            .addServiceUuid(ParcelUuid(UUID(0x123abcL, -1L)), null)
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            // Find only devices that match this request filter.
            .addDeviceFilter(deviceFilter)
            // Stop scanning as soon as one device matching the filter is found.
            .setSingleDevice(true)
            .build()

        val deviceManager: CompanionDeviceManager = getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        val executor: Executor =  Executor { it.run() }

        deviceManager.associate(pairingRequest,
            executor,
            object : CompanionDeviceManager.Callback() {
                // Called when a device is found. Launch the IntentSender so the user
                // can select the device they want to pair with.
                override fun onAssociationPending(intentSender: IntentSender) {
                    intentSender?.let {
                        startIntentSenderForResult(it, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
                    }
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    // An association is created.
                }

                override fun onFailure(errorMessage: CharSequence?) {
                    // To handle the failure.
                }
            })






        binding.back.setOnClickListener { finish() }
        binding.save.setOnClickListener { save() }
        binding.delete.setOnClickListener { delete() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE -> when(resultCode) {
                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->
                        device.createBond()
                        // Continue to interact with the paired device.
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun save() {
        val nick = binding.etNickPrinter.text.toString()

        //TODO BluetoothDevice Alias 바꾸기
        printer.setAlias(nick)
        Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
        finish()
    }

    fun delete() {
        // 삭제 전 연결 해제
        if (MyApplication.bluetoothPort.isConnected) MyApplication.bluetoothPort.disconnect()

        // TODO 연결 해제 > 페어링 해제 > remote list에서 제거 > finish()
    }
}