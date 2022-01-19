package cn.mtjsoft.cameraxbarcodescanning

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import cn.mtjsoft.barcodescanning.ScanningActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)

        findViewById<TextView>(R.id.openPreview).setOnClickListener {
            ScanningActivity.openScan(this)
        }
    }
}