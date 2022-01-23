package cn.mtjsoft.barcodescanning

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.core.app.ActivityCompat
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.config.ScanType
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

/**
 * @author mtj
 * @date 2022/1/20
 * @desc
 * @email mtjsoft3@gmail.com
 */
class ScanningManager private constructor() {

    private val TAG = "ScanningManager"

    companion object {
        val instance: ScanningManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ScanningManager()
        }
    }

    private var config: Config = Config()

    private var mBarcodeScannerCodeBar: BarcodeScanner? = null

    private var mBarcodeScannerQrCode: BarcodeScanner? = null

    /**
     * 打开预览扫描识别
     */
    fun openScanningActivity(context: Context, config: Config = Config()) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        this.config = config
        val intent = Intent(context, ScanningActivity::class.java)
        context.startActivity(intent)
    }

    fun getConfig() = config

    fun getBarcodeScanningClient(@ScanType scanType: Int = ScanType.QR_CODE): BarcodeScanner {
        when (scanType) {
            ScanType.CODE_BAR -> {
                if (mBarcodeScannerCodeBar == null) {
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_CODE_39,
                            Barcode.FORMAT_CODE_93,
                            Barcode.FORMAT_CODABAR,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_AZTEC
                        )
                        .build()
                    mBarcodeScannerCodeBar = BarcodeScanning.getClient(options)
                }
                return mBarcodeScannerCodeBar!!
            }
            else -> {
                if (mBarcodeScannerQrCode == null) {
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE
                        )
                        .build()
                    mBarcodeScannerQrCode = BarcodeScanning.getClient(options)
                }
                return mBarcodeScannerQrCode!!
            }
        }
    }

    /**
     * 从文件的Uri直接扫描识别
     */
    fun scanningByUri(context: Context, uri: Uri, scanResultListener: ScanResultListener? = null) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            scanning(image, scanResultListener)
        } catch (e: IOException) {
            e.printStackTrace()
            scanResultListener?.onFailureListener(e.message ?: "scan failed")
            scanResultListener?.onCompleteListener()
        }
    }

    /**
     * 从Bitmap直接扫描识别
     */
    fun scanningByBitmap(bitmap: Bitmap, scanResultListener: ScanResultListener? = null) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            scanning(image, scanResultListener)
        } catch (e: Exception) {
            scanResultListener?.onFailureListener(e.message ?: "scan failed")
            scanResultListener?.onCompleteListener()
        }
    }

    /**
     * 开始扫描识别
     */
    private fun scanning(image: InputImage, scanResultListener: ScanResultListener? = null) {
        try {
            getBarcodeScanningClient().process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    successResult(false, barcodes, scanResultListener)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    scanResultListener?.onFailureListener(it.message ?: "scan failed")
                }
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result.isNotEmpty()) {
                        successResult(true, it.result, scanResultListener)
                    } else {
                        scanResultListener?.onCompleteListener()
                    }
                }
        } catch (e: Exception) {
            scanResultListener?.onFailureListener(e.message ?: "scan failed")
            scanResultListener?.onCompleteListener()
        }
    }

    private fun successResult(
        isComplete: Boolean,
        barcodes: List<Barcode>,
        scanResultListener: ScanResultListener? = null
    ) {
        if (barcodes.isEmpty()) {
            scanResultListener?.onFailureListener("scan result isEmpty")
            return
        }
        // 返回识别区域最大的
        val barcode = getRectMaxResult(barcodes)
        if (isComplete) {
            scanResultListener?.onCompleteListener(barcode?.rawValue)
        } else {
            scanResultListener?.onSuccessListener(barcode?.rawValue)
        }
    }

    /**
     * 获取最大范围的条码结果
     */
    fun getRectMaxResult(barcodes: List<Barcode>): Barcode? {
        // 返回识别区域最大的
        if (barcodes.isEmpty()) {
            return null
        }
        var areaMax = 0
        var indexMax = 0
        barcodes.forEachIndexed { index, barcode ->
            barcode.boundingBox?.let {
                val area = it.width() * it.height()
                if (area > areaMax && !barcode.rawValue.isNullOrEmpty()) {
                    areaMax = area
                    indexMax = index
                }
            }
        }
        return barcodes[indexMax]
    }

    private var scaleX = 0f
    private var scaleY = 0f

    //初始化缩放比例
    fun initScale(scanViewWidth: Int, scanViewHeight: Int, imageWidth: Int, imageHeight: Int) {
        scaleX = scanViewWidth.toFloat() / imageWidth.toFloat()
        scaleY = scanViewHeight.toFloat() / imageHeight.toFloat()
    }

    //将扫描的矩形换算为当前屏幕大小
    fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    private fun translateX(x: Float): Float = x * scaleX
    private fun translateY(y: Float): Float = y * scaleY
}