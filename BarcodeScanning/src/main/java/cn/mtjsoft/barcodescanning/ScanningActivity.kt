package cn.mtjsoft.barcodescanning

import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.utils.MainThreadAsyncHandler
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import cn.mtjsoft.barcodescanning.utils.ScanUtil
import cn.mtjsoft.barcodescanning.utils.ScanUtil.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.gyf.immersionbar.ImmersionBar
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author mtj
 * @date 2022/1/19
 * @desc
 * @email mtjsoft3@gmail.com
 */
class ScanningActivity : AppCompatActivity() {

    private val TAG: String = "ScanningActivity"
    private val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        1, 20, 3, TimeUnit.SECONDS,
        SynchronousQueue<Runnable>()
    )
    private var torchState = TorchState.OFF
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private var mCameraControl: CameraControl? = null

    private lateinit var lowLightView: TextView

    companion object {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
        ImmersionBar.with(this)
            .transparentStatusBar()  //透明状态栏，不写默认透明色
            .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
            .transparentBar().init()
        initView()
        preview()
    }

    private fun initView() {
        previewView = findViewById(R.id.previewView)
        lowLightView = findViewById(R.id.tv_low_light)
        findViewById<TextView>(R.id.tv_open).setOnClickListener {
            enableTorch(true)
        }
        findViewById<TextView>(R.id.tv_close).setOnClickListener {
            enableTorch(false)
        }
    }

    /**
     * 闪光灯开启/关闭操作
     */
    private fun enableTorch(open: Boolean) {
        when {
            open && torchState == TorchState.OFF -> {
                mCameraControl?.enableTorch(true)
            }
            !open && torchState == TorchState.ON -> {
                mCameraControl?.enableTorch(false)
            }
        }
    }

    private fun preview() {
        // 请求 CameraProvider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // 检查 CameraProvider 可用性
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 选择相机并绑定生命周期和用例
     */
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            // enable the following line if RGBA output is needed.
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(threadPoolExecutor, { imageProxy ->
            try {
                scanning(imageProxy)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        mCameraControl = camera.cameraControl
        camera.cameraInfo.torchState.observe(this, {
            torchState = it
        })
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun scanning(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        lowLight(mediaImage)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
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
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints
                    val rawValue = barcode.rawValue
                    Log.e(TAG, "scanner rawValue: $rawValue")
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
            }
            .addOnCompleteListener {
                Log.e(TAG, "scanner OnCompleteListener isSuccessful : ${it.isSuccessful}")
                mediaImage.close()
                imageProxy.close()
            }
    }

    /**
     * 弱光环境处理
     */
    @SuppressLint("RestrictedApi")
    private fun lowLight(mediaImage: Image) {
        if (torchState == TorchState.OFF) {
            val bitmap = mediaImage.toBitmap()
            val isLowLight = bitmap?.run {
                ScanUtil.isLowLight(this)
            }
            Log.e(TAG, "是否是弱光：$isLowLight")
            if (isLowLight == true) {
                // 弱光环境，提示开启闪光灯
                MainThreadAsyncHandler.getInstance().post {
                    lowLightView.visibility = View.VISIBLE
                }
            } else {
                // 已变成非弱光环境，隐藏提示
            }
        } else {
            // 闪光灯已打开，关闭提示
        }
    }

    override fun onDestroy() {
        enableTorch(false)
        super.onDestroy()
    }
}