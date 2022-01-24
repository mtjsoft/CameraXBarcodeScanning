package cn.mtjsoft.barcodescanning.config

import androidx.annotation.IntDef
import cn.mtjsoft.barcodescanning.interfaces.ImageEngines
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import java.io.Serializable

/**
 * @author mtj
 * @date 2022/1/21
 * @desc
 * @email mtjsoft3@gmail.com
 */
data class Config(
    /**
     * 是否开启多码识别
     * 不开启时，取最大范围的
     */
    val enabled: Boolean = true,
    /**
     * 默认扫码类型
     * 二维码 / 条形码
     */
    @ScanType
    val scanType: Int = ScanType.QR_CODE,

    /**
     * 图库图片加载引擎
     */
    val mImageEngines: ImageEngines? = null,
    /**
     * 扫码回调
     */
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
