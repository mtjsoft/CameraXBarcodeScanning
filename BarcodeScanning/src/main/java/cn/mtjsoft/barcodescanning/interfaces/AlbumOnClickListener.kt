package cn.mtjsoft.barcodescanning.interfaces

import android.net.Uri
import android.view.View
import java.io.Serializable

interface AlbumOnClickListener : Serializable {
    fun onClick(v: View, callBack: CallBackFileUri)
}

interface CallBackFileUri : Serializable {
    fun callBackUri(uri: Uri)
}