package cn.mtjsoft.cameraxbarcodescanning

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.config.ScanType
import cn.mtjsoft.barcodescanning.interfaces.AlbumOnClickListener
import cn.mtjsoft.barcodescanning.interfaces.CallBackFileUri
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.luck.picture.lib.utils.SandboxTransformUtils
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            100
        )

        resultView = findViewById(R.id.tv_result)


        findViewById<Button>(R.id.openPreview).setOnClickListener {
            startScann()
        }
    }

    /**
     * 开始扫描
     */
    private fun startScann() {
        ScanningManager.instance.openScanningActivity(
            this,
            Config(
                true,
                ScanType.QR_CODE,
                object : AlbumOnClickListener {
                    override fun onClick(v: View, callBack: CallBackFileUri) {
                        // 1、检查申请必要的权限

                        // 2、相册选完图片，回调uri进行识别
                        pictureSelector(callBack)
                    }
                },
                object : ScanResultListener {
                    override fun onSuccessListener(value: String?) {
                        resultView.text = "扫码结果： \n$value"
                    }

                    override fun onFailureListener(error: String) {
                    }

                    override fun onCompleteListener(value: String?) {
                    }
                })
        )
    }

    /**
     * 选择图片
     *
     */
    private fun pictureSelector(callBack: CallBackFileUri) {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setSelectionMode(SelectModeConfig.SINGLE)
            .isDisplayCamera(false)
            .isDirectReturnSingle(true)
            .setSelectorUIStyle(PictureSelectorStyle().apply {
                windowAnimationStyle = PictureWindowAnimationStyle(0, 0)
            })
            .setImageEngine(GlideEngine.createGlideEngine())
            // 访问沙盒外资源
            .setSandboxFileEngine { context, srcPath, mineType, call ->
                if (call != null) {
                    val sandboxPath =
                        SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
                    call.onCallback(srcPath, sandboxPath)
                }
            }
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result.isNullOrEmpty() || result[0] == null) {
                        return
                    }
                    result[0]?.let { media ->
                        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Uri.fromFile(File(media.sandboxPath))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                this@MainActivity, "$packageName.fileprovider",
                                File(media.realPath)
                            )
                        } else {
                            Uri.fromFile(File(media.realPath))
                        }
                        callBack.callBackUri(uri)
                    }
                }

                override fun onCancel() {
                }
            })
    }
}