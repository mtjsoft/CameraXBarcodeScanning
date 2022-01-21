package cn.mtjsoft.barcodescanning.config

import cn.mtjsoft.barcodescanning.interfaces.ConfigInterface
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import java.io.Serializable

/**
 * @author mtj
 * @date 2022/1/21
 * @desc
 * @email mtjsoft3@gmail.com
 */
data class Config(val enabled: Boolean = true) : Serializable, ConfigInterface {
    private var scanResultListener: ScanResultListener? = null

    override fun setScanResultListener(scanResultListener: ScanResultListener): Config {
        this.scanResultListener = scanResultListener
        return this
    }

    fun getScanResultListener() = this.scanResultListener
}
