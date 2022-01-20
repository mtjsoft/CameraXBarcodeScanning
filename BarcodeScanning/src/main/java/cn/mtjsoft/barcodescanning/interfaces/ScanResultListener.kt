package cn.mtjsoft.barcodescanning.interfaces

import java.io.Serializable

/**
 * @author mtj
 * @date 2022/1/20
 * @desc 扫描结果回调接口
 * @email mtjsoft3@gmail.com
 */
interface ScanResultListener : Serializable {
    fun onSuccessListener(value: String? = null)
    fun onFailureListener(error: String)
    fun onCompleteListener(value: String? = null)
}