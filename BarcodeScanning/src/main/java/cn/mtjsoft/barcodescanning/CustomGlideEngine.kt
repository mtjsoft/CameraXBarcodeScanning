package cn.mtjsoft.barcodescanning

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.interfaces.OnCallbackListener

/**
 * @author mtj
 * @describe：Glide加载引擎
 */
class CustomGlideEngine private constructor() : ImageEngine {
    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            loadImage(context, url, imageView)
        }
    }

    /**
     * 加载指定url并返回bitmap
     *
     * @param context 上下文
     * @param url     资源url
     * @param call    回调接口
     */
    override fun loadImageBitmap(
        context: Context,
        url: String,
        maxWidth: Int,
        maxHeight: Int,
        call: OnCallbackListener<Bitmap>
    ) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            loadImageBitmap(
                context, url, maxWidth, maxHeight
            ) {
                call.onCall(it)
            }
        }
    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            loadAlbumCover(context, url, imageView)
        }
    }

    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            loadGridImage(context, url, imageView)
        }
    }

    override fun pauseRequests(context: Context) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            pauseRequests(context)
        }
    }

    override fun resumeRequests(context: Context) {
        ScanningManager.instance.getConfig().mImageEngines?.apply {
            resumeRequests(context)
        }
    }

    companion object {
        val instance: CustomGlideEngine by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CustomGlideEngine()
        }
    }
}