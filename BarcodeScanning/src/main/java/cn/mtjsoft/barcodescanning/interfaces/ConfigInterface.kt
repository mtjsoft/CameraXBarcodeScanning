package cn.mtjsoft.barcodescanning.interfaces

/**
 * @author mtj
 * @date 2022/1/20
 * @desc
 * @email mtjsoft3@gmail.com
 */
interface ConfigInterface {
    fun setScanResultListener(scanResultListener: ScanResultListener): ConfigInterface
}