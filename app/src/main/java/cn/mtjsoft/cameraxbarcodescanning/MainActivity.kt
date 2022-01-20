package cn.mtjsoft.cameraxbarcodescanning

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.callback.Builder
import cn.mtjsoft.barcodescanning.callback.ScanResultListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)

        findViewById<TextView>(R.id.openPreview).setOnClickListener {
            ScanningManager.instance.openScanningActivity(
                this
            )
        }
    }
}