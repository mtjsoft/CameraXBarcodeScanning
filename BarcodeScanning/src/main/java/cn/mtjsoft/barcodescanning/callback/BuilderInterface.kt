package cn.mtjsoft.barcodescanning.callback

import java.io.Serializable

/**
 * @author mtj
 * @date 2022/1/20
 * @desc
 * @email mtjsoft3@gmail.com
 */
interface BuilderInterface {
    fun setScanResultListener(scanResultListener: ScanResultListener): BuilderInterface

    fun setScanMultiCodeEnabled(enabled: Boolean): BuilderInterface
}

class Builder() : BuilderInterface, Serializable {

    private var scanResultListener: ScanResultListener? = null

    private var enabled: Boolean = false

    override fun setScanResultListener(scanResultListener: ScanResultListener): Builder {
        this.scanResultListener = scanResultListener
        return this
    }

    override fun setScanMultiCodeEnabled(enabled: Boolean): Builder {
        this.enabled = enabled
        return this
    }
}