package cn.mtjsoft.cameraxbarcodescanning

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)

        resultView = findViewById(R.id.tv_result)

        findViewById<Button>(R.id.openPreview).setOnClickListener {
            ScanningManager.instance.openScanningActivity(
                this,
                Config(true)
                    .setScanResultListener(object : ScanResultListener {
                        override fun onSuccessListener(value: String?) {
                            resultView.text = "扫码结果： $value"
                        }

                        override fun onFailureListener(error: String) {
                        }

                        override fun onCompleteListener(value: String?) {
                        }
                    })
            )
        }
    }
}