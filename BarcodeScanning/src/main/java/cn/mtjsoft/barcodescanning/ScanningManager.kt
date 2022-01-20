package cn.mtjsoft.barcodescanning

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.ActivityCompat
import cn.mtjsoft.barcodescanning.interfaces.Builder
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
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

    /**
     * 打开预览扫描识别
     */
    fun openScanningActivity(context: Context, builder: Builder = Builder()) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val intent = Intent(context, ScanningActivity::class.java)
        intent.putExtra("builder", builder)
        context.startActivity(intent)
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
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODABAR
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    successResult(false, barcodes, scanResultListener)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    scanResultListener?.onFailureListener(it.message ?: "scan failed")
                    scanResultListener?.onCompleteListener()
                }
                .addOnCompleteListener {
                    if (it.isSuccessful) {
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
            scanResultListener?.onCompleteListener()
            return
        }
        // 返回识别区域最大的
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
        if (isComplete) {
            scanResultListener?.onCompleteListener(barcodes[indexMax].rawValue)
        } else {
            scanResultListener?.onSuccessListener(barcodes[indexMax].rawValue)
        }
    }
}