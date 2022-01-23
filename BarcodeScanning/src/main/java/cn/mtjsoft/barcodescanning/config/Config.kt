package cn.mtjsoft.barcodescanning.config

import androidx.annotation.IntDef
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import java.io.Serializable

/**
 * @author mtj
 * @date 2022/1/21
 * @desc
 * @email mtjsoft3@gmail.com
 */
data class Config(
    val enabled: Boolean = true,
    @ScanType
    val scanType: Int = ScanType.QR_CODE,
    val scanResultListener: ScanResultListener? = null
) : Serializable {
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@IntDef(ScanType.QR_CODE, ScanType.CODE_BAR)
annotation class ScanType {
    companion object {
        const val QR_CODE = 0
        const val CODE_BAR = 1
    }
}
