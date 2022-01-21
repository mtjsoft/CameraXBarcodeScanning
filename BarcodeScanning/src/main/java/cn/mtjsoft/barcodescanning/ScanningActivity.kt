package cn.mtjsoft.barcodescanning

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.extentions.screenHeight
import cn.mtjsoft.barcodescanning.extentions.screenWidth
import cn.mtjsoft.barcodescanning.interfaces.CustomTouchListener
import cn.mtjsoft.barcodescanning.utils.ScanUtil
import cn.mtjsoft.barcodescanning.utils.ScanUtil.toBitmap
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import cn.mtjsoft.barcodescanning.view.CustomGestureDetectorView
import com.google.common.util.concurrent.ListenableFuture
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
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private var mCameraControl: CameraControl? = null
    private var mCameraInfo: CameraInfo? = null

    // 闪光灯开启/关闭
    private var torchState = TorchState.OFF

    // 缩放状态
    private var mZoomState: ZoomState? = null

    // 手势缩放步长
    private val zoomStep = 0.1f

    // 是否开启扫码
    @Volatile
    private var scanEnable = true

    private var config: Config = Config()

    private lateinit var lowLightView: TextView
    private lateinit var lineImageView: ImageView
    private lateinit var mCustomGestureDetectorView: CustomGestureDetectorView

    private var widthPixels = 0
    private var heightPixels = 0
    private val animationSet = AnimatorSet()

    companion object {
        val mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
        ImmersionBar.with(this).transparentBar().init()
        initView()
        SoundPoolUtil.instance.loadQrcodeCompletedWav(this)
        preview()
    }

    private fun initView() {
        config = ScanningManager.instance.getConfig()
        widthPixels = this.screenWidth
        heightPixels = this.screenHeight
        previewView = findViewById(R.id.previewView)
        lowLightView = findViewById(R.id.tv_low_light)
        lineImageView = findViewById(R.id.iv_scan_line)
        findViewById<TextView>(R.id.tv_open).setOnClickListener {
            enableTorch(true)
        }
        findViewById<TextView>(R.id.tv_close).setOnClickListener {
            enableTorch(false)
        }
        mCustomGestureDetectorView = findViewById(R.id.gestureDetectorView)
        mCustomGestureDetectorView.setCustomTouchListener(customTouchListener)
        startScanLineAnimator()
    }

    @SuppressLint("Recycle")
    private fun startScanLineAnimator() {
        animationSet.cancel()
        lineImageView.visibility = View.VISIBLE
        val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(lineImageView, "alpha", 0.2f, 1f, 1f, 0f)
        alphaAnimator.repeatCount = INFINITE
        val translationYAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(lineImageView, "translationY", 0f, heightPixels / 3f * 2)
        translationYAnimator.repeatCount = INFINITE
        animationSet.playTogether(alphaAnimator, translationYAnimator)
        animationSet.duration = 3000
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.start()
    }

    private fun stopScanLineAnimator() {
        animationSet.cancel()
        lineImageView.visibility = View.GONE
    }

    /**
     * 请求 CameraProvider
     * 检查 CameraProvider 可用性
     */
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
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(threadPoolExecutor, { imageProxy ->
            try {
                if (scanEnable) {
                    scanning(imageProxy)
                } else {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageProxy.close()
            }
        })
        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
        mCameraControl = camera.cameraControl
        mCameraInfo = camera.cameraInfo
        mZoomState = mCameraInfo?.zoomState?.value
        mCameraInfo?.torchState?.observe(this, {
            torchState = it
        })
        mCameraInfo?.zoomState?.observe(this, {
            mZoomState = it
        })
    }

    /**
     * 处理扫描结果
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun scanning(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        lowLight(mediaImage)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        ScanningManager.instance.getBarcodeScanningClient().process(image)
            .addOnSuccessListener { barcodes ->
                when {
                    barcodes.size == 1 -> {
                        SoundPoolUtil.instance.playQrcodeCompleted()
                        mCustomGestureDetectorView.addScanView(image, barcodes) {
                        }
                        resultOk(barcodes[0], 1000)
                    }
                    barcodes.size > 1 -> {
                        SoundPoolUtil.instance.playQrcodeCompleted()
                        // 标记多个码位置
                        if (config.enabled) {
                            mCustomGestureDetectorView.addScanView(image, barcodes) {
                                resultOk(it)
                            }
                        } else {
                            // 取最大范围的
                            ScanningManager.instance.getRectMaxResult(barcodes)?.let {
                                mCustomGestureDetectorView.addScanView(image, arrayListOf(it)) {
                                }
                                resultOk(it, 1000)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
            }
            .addOnCompleteListener {
                Log.e(TAG, "scanner OnCompleteListener isSuccessful : ${it.isSuccessful}  resultSize: ${it.result.size}")
                // 扫码成功，且有结果时，停止扫码
                if (it.isSuccessful && it.result.isNotEmpty()) {
                    scanEnable = false
                    stopScanLineAnimator()
                    // 解除绑定，停止预览
                    cameraProviderFuture.get().unbindAll()
                }
                mediaImage.close()
                imageProxy.close()
                Log.e(TAG, "view.width : ${mCustomGestureDetectorView.width}  ,view.height: ${mCustomGestureDetectorView.height}")
                Log.e(
                    TAG,
                    "image.width : ${image.width}  ,image.height: ${image.height} ,image.rotationDegrees: ${image.rotationDegrees}"
                )
            }
    }

    /**
     * 回调结果，关闭界面
     */
    private fun resultOk(barcode: Barcode?, timeMillis: Long = 0) {
        barcode?.let {
            mainHandler.postDelayed({
                config.getScanResultListener()?.apply {
                    onSuccessListener(it.rawValue)
                    onCompleteListener(it.rawValue)
                }
                finish()
            }, timeMillis)
        }
    }

    /**
     * 弱光环境处理
     */
    private fun lowLight(mediaImage: Image) {
        if (torchState == TorchState.OFF) {
            val bitmap = mediaImage.toBitmap()
            val isLowLight = bitmap?.run {
                ScanUtil.isLowLight(this)
            }
            Log.e(TAG, "是否是弱光：$isLowLight")
            if (isLowLight == true) {
                // 弱光环境，提示开启闪光灯
                mainHandler.post {
                    lowLightView.visibility = View.VISIBLE
                }
            } else {
                // 已变成非弱光环境，隐藏提示
            }
        } else {
            // 闪光灯已打开，关闭提示
        }
    }

    /**
     * 手势缩放 | 双击缩放 | 点击对焦
     */
    private val customTouchListener: CustomTouchListener = object : CustomTouchListener {
        override fun zoom() {
            // 双指手势放大
            mZoomState?.apply {
                if (zoomRatio < maxZoomRatio) {
                    val curZoomRatio = zoomRatio + zoomStep
                    if (curZoomRatio <= maxZoomRatio) {
                        mCameraControl?.setZoomRatio(curZoomRatio)
                    } else {
                        mCameraControl?.setZoomRatio(maxZoomRatio)
                    }
                }
            }
        }

        override fun ZoomOut() {
            // 双指手势缩小
            mZoomState?.apply {
                if (zoomRatio > minZoomRatio) {
                    val curZoomRatio = zoomRatio - zoomStep
                    if (curZoomRatio >= minZoomRatio) {
                        mCameraControl?.setZoomRatio(curZoomRatio)
                    } else {
                        mCameraControl?.setZoomRatio(minZoomRatio)
                    }
                }
            }
        }

        override fun click(x: Float, y: Float) {
            // 点击时，启用对焦
            clickFocus(x, y)
        }

        override fun doubleClick(x: Float, y: Float) {
            // 双击放大，或缩小
            mZoomState?.apply {
                if (linearZoom > 0) {
                    mCameraControl?.setLinearZoom(0f)
                } else {
                    mCameraControl?.setLinearZoom(0.5f)
                }
            }
        }

        override fun longClick(x: Float, y: Float) {
        }
    }

    /**
     * 点按对焦
     */
    private fun clickFocus(x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(previewView.width.toFloat(), previewView.height.toFloat())
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        mCameraControl?.startFocusAndMetering(action)?.addListener(Runnable {
            // 对焦结束
        }, ContextCompat.getMainExecutor(this))
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

    override fun onDestroy() {
        stopScanLineAnimator()
        enableTorch(false)
        super.onDestroy()
    }
}