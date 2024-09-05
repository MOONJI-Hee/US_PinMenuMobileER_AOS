package com.wooriyo.us.pinmenumobileer.util

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.widget.Toast
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.cmd.Cmd
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.CommonEnum.ALIGN_LEFT
import com.rt.printerlibrary.enumerate.CommonEnum.ALIGN_RIGHT
import com.rt.printerlibrary.enumerate.CommonEnum.FontStyle
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.printer.ThermalPrinter
import com.rt.printerlibrary.setting.TextSetting
import com.sewoo.jpos.command.ESCPOSConst
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bluetoothAdapter
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pairedDevices
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.remoteDevices
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.RT_TITLE_BIG
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.RT_TITLE_RECEIPT
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.RT_TITLE_SMALL
import com.wooriyo.us.pinmenumobileer.config.AppProperties.Companion.TITLE_MENU
import com.wooriyo.us.pinmenumobileer.model.OrderDTO
import com.wooriyo.us.pinmenumobileer.model.OrderHistoryDTO
import com.wooriyo.us.pinmenumobileer.util.AppHelper.Companion.price
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern

// 블루투스 & 프린터 연결 관련 메소드
class PrinterHelper {
    companion object {
        fun checkSewoo(bluetoothDevice: BluetoothDevice): Boolean {
            return when(bluetoothDevice.address.substring(0, 8)) {
                "60:6E:41" -> false
                "00:13:7B" -> true
                else -> false
            }
        }

        fun connRT(context: Context, bluetoothDevice: BluetoothDevice) {
            if(remoteDevices.isEmpty()) {
                Toast.makeText(context, "There is no connectable Bluetooth device.", Toast.LENGTH_SHORT).show()
                return
            }

            val configObj = BluetoothEdrConfigBean(bluetoothDevice)
            val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean

            val piFactory: PIFactory = BluetoothFactory()
            val printerInterface = piFactory.create() as PrinterInterface

            printerInterface.configObject = bluetoothEdrConfigBean
            MyApplication.rtPrinter.setPrinterInterface(printerInterface)

            try {
                (MyApplication.rtPrinter as ThermalPrinter).connect(bluetoothEdrConfigBean)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("PrinterHelper", "RP325 Connect Error > $e")
                Toast.makeText(context, "Bluetooth Connection Fail", Toast.LENGTH_SHORT).show()
            }
        }

        fun connSewoo(context: Context, bluetoothDevice: BluetoothDevice) {
            Log.d("PrinterHelper", "세우테크 프린터 커넥트 시작")

            if(MyApplication.remoteDevices.isEmpty()) {
                Toast.makeText(context, "There is no connectable Bluetooth device.", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                MyApplication.bluetoothPort.connect(bluetoothDevice)
                MyApplication.connDev_sewoo = bluetoothDevice.address

                val rh = RequestHandler()
                MyApplication.btThread = Thread(rh)
                MyApplication.btThread!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("PrinterHelper", "Sewoo Connect Error > $e")
                Toast.makeText(context, "Bluetooth Connection Fail", Toast.LENGTH_SHORT).show()
            }
        }

        // 페어링 된 기기 찾기
        fun getPairedDevice() : Int {
            Log.d("AppHelper", "getPairedDevice 시작")
            pairedDevices.clear()
            remoteDevices.clear()

            val foundDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            foundDevices?.forEach { device ->
                if(MyApplication.bluetoothPort.isValidAddress(device.address)) {
                    val deviceNum = device.bluetoothClass.majorDeviceClass

                    if(deviceNum == BluetoothClass.Device.Major.IMAGING) {
                        pairedDevices.add(device)
                        remoteDevices.add(device)
                    }
                }
            }
            Log.d("AppHelper", "페어링된 기기 목록 >> $pairedDevices")

            return if(pairedDevices.isNotEmpty()) 1 else 0
        }

        fun print(order: OrderHistoryDTO, context: Context) {
            if (MyApplication.rtPrinter.getPrinterInterface() != null && MyApplication.rtPrinter.connectState == ConnectStateEnum.Connected) {
                if(MyApplication.store.kitchen == "Y") printRT(order, context)
                if(MyApplication.store.receipt == "Y") printReceipt(order)
            }
            if(MyApplication.bluetoothPort.isConnected) {
                printSewoo(order, context)
            }
        }

        fun printRT(order: OrderHistoryDTO, context: Context) {
            val hyphen = StringBuilder()

            for (i in 1..48) {
                hyphen.append("-")
            }

            val escFac : CmdFactory = EscFactory()
            val escCmd : Cmd = escFac.create()
            escCmd.chartsetName = "UTF-8"

            val defaultText = TextSetting().apply {
                align = CommonEnum.ALIGN_LEFT
            }

            val smallText = TextSetting().apply {
                escFontType = ESCFontTypeEnum.FONT_A_12x24
                align = CommonEnum.ALIGN_LEFT
                isEscSmallCharactor = SettingEnum.Enable
            }

            val largeText = TextSetting().apply {
                escFontType = ESCFontTypeEnum.FONT_A_12x24
                align = CommonEnum.ALIGN_MIDDLE
                doubleWidth = SettingEnum.Enable
                doubleHeight = SettingEnum.Enable
            }

            val textSetting = TextSetting().apply {
                escFontType = ESCFontTypeEnum.FONT_A_12x24
                align = CommonEnum.ALIGN_LEFT
            }

            var title = RT_TITLE_SMALL
            if(MyApplication.store.fontsize == 1) {
                textSetting.doubleWidth = SettingEnum.Enable
                title = RT_TITLE_BIG
            }

            var strReser = ""
            if(order.reserType > 0 && order.rlist.isNotEmpty()) {
                when(order.reserType) {
                    1 -> strReser = "Res."
                    2 -> strReser = "To-go"
                }
            }else {
                strReser = "Dine-in #${order.tableNo}"
            }

            try {
                escCmd.append(escCmd.getTextCmd(largeText, strReser))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(textSetting, order.regdt))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(textSetting, "Order No   : ${order.ordcode}"))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(textSetting, "Table No   : ${order.tableNo}\n"))
                escCmd.append(escCmd.lfcrCmd)

                escCmd.append(escCmd.getTextCmd(textSetting, title))
                escCmd.append(escCmd.lfcrCmd)

                escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                escCmd.append(escCmd.lfcrCmd)

                order.olist.forEach {
                    val pOrder = getPrintRT(it)
                    escCmd.append(escCmd.getTextCmd(textSetting, pOrder))
                    escCmd.append(escCmd.lfcrCmd)
                    escCmd.append(escCmd.getTextCmd(smallText, "\n"))
                }

                if(order.paytype == 3) {
                    escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                    escCmd.append(escCmd.lfcrCmd)
                    defaultText.doubleHeight = SettingEnum.Enable
                    escCmd.append(escCmd.getTextCmd(defaultText, "Complete payment"))
                    defaultText.doubleHeight = SettingEnum.Disable
                    escCmd.append(escCmd.lfcrCmd)
                }

                if(order.reserType > 0 && order.rlist.isNotEmpty()) {
                    val reserv = order.rlist[0]

                    escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                    escCmd.append(escCmd.lfcrCmd)

                    escCmd.append(escCmd.getTextCmd(smallText, "Phone Num"))
                    escCmd.append(escCmd.lfcrCmd)

                    escCmd.append(escCmd.getTextCmd(textSetting, reserv.tel))
                    escCmd.append(escCmd.lfcrCmd)

                    escCmd.append(escCmd.getTextCmd(smallText, "Res. Name"))
                    escCmd.append(escCmd.lfcrCmd)

                    escCmd.append(escCmd.getTextCmd(textSetting, reserv.name))
                    escCmd.append(escCmd.lfcrCmd)

                    if(reserv.memo.isNotEmpty()) {
                        escCmd.append(escCmd.getTextCmd(smallText, "Request"))
                        escCmd.append(escCmd.lfcrCmd)

                        escCmd.append(escCmd.getTextCmd(textSetting, reserv.memo))
                        escCmd.append(escCmd.lfcrCmd)
                    }

                    var str = ""
                    when(order.reserType) {
                        1 -> str = "Store"
                        2 -> str = "To-go"
                    }

                    escCmd.append(escCmd.getTextCmd(smallText, String.format(context.getString(R.string.reserv_date), str)))
                    escCmd.append(escCmd.lfcrCmd)

                    escCmd.append(escCmd.getTextCmd(textSetting, reserv.reserdt))
                    escCmd.append(escCmd.lfcrCmd)
                }

                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(largeText, strReser))
                escCmd.append(escCmd.lfcrCmd)

                escCmd.append(escCmd.cmdCutNew)

                MyApplication.rtPrinter.writeMsgAsync(escCmd.appendCmds)

            } catch (e : UnsupportedEncodingException) {
                e.printStackTrace()
                Log.d("AppHelper", "Exception > $e")

            }
        }

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - RTP325
        fun getPrintRT(ord: OrderDTO) : String {
            val oneLine = AppProperties.RT_ONE_LINE
            val productLine = AppProperties.RT_PRODUCT
            val qtyLine = AppProperties.RT_QTY

            val result: StringBuilder = StringBuilder()
            val underline1 = StringBuilder()
            val underline2 = StringBuilder()
            val underline3 = StringBuilder()

            var total = 1
            ord.name.forEach {
                if (total <= productLine)
                    result.append(it)
                else if (total <= (productLine * 2))
                    underline1.append(it)
                else if (total <= (productLine * 3))
                    underline2.append(it)
                else
                    underline3.append(it)

                total++
            }

            val diff1 = productLine - (result.toString().length)
            for(i in 1..diff1) {
                result.append(" ")
            }

            result.append(" ")

            if (ord.gea < 100) result.append(" ")

            result.append(ord.gea)

            if(ord.gea < 10) result.append(" ")

            if (underline1.toString() != "")
                result.append("\r$underline1")

            if (underline2.toString() != "")
                result.append("\r$underline2")

            if(!ord.opt.isNullOrEmpty()) {
                ord.opt.forEach {
                    result.append("\n -$it")
                }
            }

            return result.toString()
        }

        fun printReceipt(order: OrderHistoryDTO) {
            val escFac : CmdFactory = EscFactory()
            val escCmd : Cmd = escFac.create()
            escCmd.chartsetName = "UTF-8"

            val defaultText = TextSetting().apply {
                align = CommonEnum.ALIGN_LEFT
            }

            val hyphen = StringBuilder()

            for (i in 1..48) {
                hyphen.append("-")
            }

            try {
                // title (매장 이름, 매장 주소, 매장 번호)
                escCmd.append(escCmd.getTextCmd(defaultText, MyApplication.store.name))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(defaultText, MyApplication.store.address))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(defaultText, PhoneNumberUtils.formatNumber(MyApplication.store.tel, "US")))
                escCmd.append(escCmd.lfcrCmd)

                // 주문 날짜
                escCmd.append(escCmd.getTextCmd(defaultText, order.regdt))
                escCmd.append(escCmd.lfcrCmd)

                // 하이픈
                escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                escCmd.append(escCmd.lfcrCmd)

                // 타이틀 (Product Qty Amt)
                escCmd.append(escCmd.getTextCmd(defaultText, RT_TITLE_RECEIPT))
                escCmd.append(escCmd.lfcrCmd)

                // 하이픈
                escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                escCmd.append(escCmd.lfcrCmd)

                // 주문내역
                order.olist.forEach {
                    val pOrder = getReceiptPrint(it)
                    escCmd.append(escCmd.lfcrCmd)
                    escCmd.append(escCmd.getTextCmd(defaultText, pOrder))
                    escCmd.append(escCmd.lfcrCmd)
                }

                // 하이픈
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                escCmd.append(escCmd.lfcrCmd)

                // 가격
                escCmd.append(escCmd.getTextCmd(defaultText, "SubTotal : $${price(order.amount)}"))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(defaultText, "Tip : $${order.tip}"))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(defaultText, "Tax : $${order.tax}"))
                escCmd.append(escCmd.lfcrCmd)

                // 하이픈
                escCmd.append(escCmd.getTextCmd(defaultText, hyphen.toString()))
                escCmd.append(escCmd.lfcrCmd)

                defaultText.bold = SettingEnum.Enable
                defaultText.doubleWidth = SettingEnum.Enable

                // 총 가격
                escCmd.append(escCmd.getTextCmd(defaultText, "Total : $${price(order.total_price)}\n\n\n"))
                escCmd.append(escCmd.lfcrCmd)

                escCmd.append(escCmd.cmdCutNew)

                MyApplication.rtPrinter.writeMsgAsync(escCmd.appendCmds)

            } catch (e : UnsupportedEncodingException) {
                e.printStackTrace()
                Log.d("AppHelper", "Exception > $e")
            }
        }

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - RTP325
        fun getReceiptPrint(ord: OrderDTO) : String {
            val productLine = 32
            val cntLine = 4
            val amtLine = 9

            val result: StringBuilder = StringBuilder()
            val underline1 = StringBuilder()
            val underline2 = StringBuilder()
            val underline3 = StringBuilder()

            var total = 1
            ord.name.forEach {
                if (total <= productLine)
                    result.append(it)
                else if (total <= (productLine * 2))
                    underline1.append(it)
                else if (total <= (productLine * 3))
                    underline2.append(it)
                else
                    underline3.append(it)

                total++
            }

            val diff1 = productLine - (result.toString().length)
            for(i in 1..diff1) {
                result.append(" ")
            }

            result.append(" ")

            val diff2 = cntLine - ord.gea.toString().length
            for(i in 1..diff2) {
                result.append(" ")
            }
            result.append(ord.gea)

            result.append(" ")

            val diff3 = amtLine - price(ord.amount).length
            for(i in 1..diff3) {
                result.append(" ")
            }
            result.append("$")
            result.append(price(ord.amount))

            if (underline1.toString() != "")
                result.append("\r$underline1")

            if (underline2.toString() != "")
                result.append("\r$underline2")

            if(!ord.opt.isNullOrEmpty()) {
                ord.opt.forEach {
                    result.append("\n - $it")
                }
            }

            return result.toString()
        }

        // 주문 프린트
        fun printSewoo(order: OrderHistoryDTO, context: Context) {
            val hyphen = StringBuilder()                // 하이픈

            for (i in 1..AppProperties.HYPHEN_NUM) {
                hyphen.append("-")
            }

            val pOrderDt = order.regdt
            val pTableNo = order.tableNo
            val pOrderNo = order.ordcode

            if(MyApplication.bluetoothPort.isConnected){
                MyApplication.escposPrinter.printAndroidFont(
                    MyApplication.store.name,
                    AppProperties.FONT_WIDTH,
                    AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont("Order Date : $pOrderDt",
                    AppProperties.FONT_WIDTH,
                    AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont("Order No : $pOrderNo",
                    AppProperties.FONT_WIDTH,
                    AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont("Table No : $pTableNo",
                    AppProperties.FONT_WIDTH,
                    AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont(
                    AppProperties.TITLE_MENU,
                    AppProperties.FONT_WIDTH,
                    AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                MyApplication.escposPrinter.printAndroidFont(hyphen.toString(),
                    AppProperties.FONT_WIDTH, AppProperties.FONT_SIZE, ESCPOSConst.LK_ALIGNMENT_LEFT)

                order.olist.forEach {
                    val pOrder = getPrintSewoo(it)
                    MyApplication.escposPrinter.printAndroidFont(pOrder,
                        AppProperties.FONT_WIDTH, AppProperties.FONT_SIZE, ESCPOSConst.LK_ALIGNMENT_LEFT)
                }

                if(order.reserType > 0 && order.rlist.isNotEmpty()) {
                    val reserv = order.rlist[0]

                    MyApplication.escposPrinter.lineFeed(2)

                    MyApplication.escposPrinter.printAndroidFont("Phone Num",
                        AppProperties.FONT_WIDTH,
                        20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                    MyApplication.escposPrinter.printAndroidFont(reserv.tel,
                        AppProperties.FONT_WIDTH,
                        33, ESCPOSConst.LK_ALIGNMENT_LEFT)
                    MyApplication.escposPrinter.printAndroidFont("Res. Name",
                        AppProperties.FONT_WIDTH,
                        20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                    MyApplication.escposPrinter.printAndroidFont(reserv.name,
                        AppProperties.FONT_WIDTH,
                        33, ESCPOSConst.LK_ALIGNMENT_LEFT)
                    MyApplication.escposPrinter.printAndroidFont("Request",
                        AppProperties.FONT_WIDTH,
                        20, ESCPOSConst.LK_ALIGNMENT_LEFT)
                    MyApplication.escposPrinter.printAndroidFont(reserv.memo,
                        AppProperties.FONT_WIDTH,
                        33, ESCPOSConst.LK_ALIGNMENT_LEFT)

                    var str = ""
                    when(order.reserType) {
                        1 -> str = "Store"
                        2 -> str = "To-go"
                    }
                    MyApplication.escposPrinter.printAndroidFont(
                        String.format(context.getString(R.string.reserv_date), str),
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

        // 주문내역(상세내역) 영수증 형태 String으로 받기 - 세우전자
        fun getPrintSewoo(ord: OrderDTO) : String {
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


    }
}