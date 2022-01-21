package cn.mtjsoft.barcodescanning.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import cn.mtjsoft.barcodescanning.R
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.extentions.dp2px
import cn.mtjsoft.barcodescanning.interfaces.CustomTouchListener
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * @author mtj
 * @date 2022/1/20
 * @desc
 * @email mtjsoft3@gmail.com
 */
class CustomGestureDetectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val mGestureDetector: GestureDetector
    private var mCustomTouchListener: CustomTouchListener? = null

    private var imageViewLableWidth = 0
    private var halfWidth = 0

    /**
     * 缩放相关
     */
    private var currentDistance = 0f
    private var lastDistance = 0f

    companion object {
        private val TAG = CustomGestureDetectorView::class.java.simpleName
    }

    fun setCustomTouchListener(customTouchListener: CustomTouchListener?) {
        mCustomTouchListener = customTouchListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 接管onTouchEvent
        return mGestureDetector.onTouchEvent(event)
    }

    private var onGestureListener: GestureDetector.OnGestureListener = object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            Log.i(TAG, "onDown: 按下")
            return true
        }

        override fun onShowPress(e: MotionEvent) {
            Log.i(TAG, "onShowPress: 刚碰上还没松开")
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.i(TAG, "onSingleTapUp: 轻轻一碰后马上松开")
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            Log.i(TAG, "onScroll: 按下后拖动")
            // 大于两个触摸点
            if (e2.pointerCount >= 2) {
                //event中封存了所有屏幕被触摸的点的信息，第一个触摸的位置可以通过event.getX(0)/getY(0)得到
                val offSetX = e2.getX(0) - e2.getX(1)
                val offSetY = e2.getY(0) - e2.getY(1)
                //运用三角函数的公式，通过计算X,Y坐标的差值，计算两点间的距离
                currentDistance = Math.sqrt((offSetX * offSetX + offSetY * offSetY).toDouble()).toFloat()
                if (lastDistance == 0f) { //如果是第一次进行判断
                    lastDistance = currentDistance
                } else {
                    if (currentDistance - lastDistance > 10) {
                        // 放大
                        mCustomTouchListener?.zoom()
                    } else if (lastDistance - currentDistance > 10) {
                        // 缩小
                        mCustomTouchListener?.ZoomOut()
                    }
                }
                //在一次缩放操作完成后，将本次的距离赋值给lastDistance，以便下一次判断
                //但这种方法写在move动作中，意味着手指一直没有抬起，监控两手指之间的变化距离超过10
                //就执行缩放操作，不是在两次点击之间的距离变化来判断缩放操作
                //故这种将本次距离留待下一次判断的方法，不能在两次点击之间使用
                lastDistance = currentDistance
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            Log.i(TAG, "onLongPress: 长按屏幕")
            mCustomTouchListener?.longClick(e.x, e.y)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            Log.i(TAG, "onFling: 滑动后松开")
            currentDistance = 0f
            lastDistance = 0f
            return true
        }
    }
    private var onDoubleTapListener: GestureDetector.OnDoubleTapListener = object : GestureDetector.OnDoubleTapListener {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.i(TAG, "onSingleTapConfirmed: 严格的单击")
            mCustomTouchListener?.click(e.x, e.y)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.i(TAG, "onDoubleTap: 双击")
            mCustomTouchListener?.doubleClick(e.x, e.y)
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            Log.i(TAG, "onDoubleTapEvent: 表示发生双击行为")
            return true
        }
    }

    init {
        mGestureDetector = GestureDetector(context, onGestureListener)
        mGestureDetector.setOnDoubleTapListener(onDoubleTapListener)
        //         mScaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
        // 解决长按屏幕无法拖动,但是会造成无法识别长按事件
        mGestureDetector.setIsLongpressEnabled(false)
        imageViewLableWidth = context.dp2px(35)
        halfWidth = imageViewLableWidth / 2
    }

    fun addScanView(image: InputImage, barcodes: List<Barcode>, result: (value: Barcode) -> Unit) {
        removeAllViews()
        if (barcodes.isNotEmpty()) {
            when (image.rotationDegrees) {
                90, 270 -> {
                    ScanningManager.instance.initScale(width, height, image.height, image.width)
                }
                else -> {
                    ScanningManager.instance.initScale(width, height, image.width, image.height)
                }
            }
        }
        barcodes.map { barcode ->
            barcode.boundingBox?.let {
                val view = ImageView(context)
                addView(view, layoutParams)
                val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                layoutParams.width = imageViewLableWidth
                layoutParams.height = imageViewLableWidth
                val rectF = ScanningManager.instance.translateRect(it)
                layoutParams.setMargins(rectF.centerX().toInt() - halfWidth, rectF.centerY().toInt() - halfWidth, 0, 0)
                view.layoutParams = layoutParams
                view.setImageResource(R.drawable.icon_scan)
                view.setOnClickListener {
                    result.invoke(barcode)
                }
            }
        }
    }
}