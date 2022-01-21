package cn.mtjsoft.barcodescanning.extentions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * 获取屏幕密度
 */
fun Context.getScreenDensity(): Float = resources.displayMetrics.density

val Context.screenHeight: Int
    get() {
        val metrics = this.resources.displayMetrics
        return metrics.heightPixels
    }

val Context.screenWidth: Int
    get() {
        val metrics = this.resources.displayMetrics
        return metrics.widthPixels
    }

/**
 * 这里获取的高度和宽度包含导航栏或状态栏的
 */
val Context.screenHeight2: Int
    @SuppressLint("ServiceCast")
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = wm.defaultDisplay
        val outPoint = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultDisplay.getRealSize(outPoint)
        } else {
            outPoint.y = screenHeight
        }
        return outPoint.y
    }

val Context.screenWidth2: Int
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = wm.defaultDisplay
        val outPoint = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultDisplay.getRealSize(outPoint)
        } else {
            outPoint.x = screenHeight
        }
        return outPoint.x
    }

val Context.statusBarHeight: Int
    get() {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

fun Context.dp2px(dp: Int): Int {
    return (dp * getScreenDensity() + 0.5f).toInt()
}

fun Context.px2dp(px: Int): Int {
    return (px / getScreenDensity() + 0.5f).toInt()
}

fun Context.px2sp(px: Int): Int {
    return (px / getScreenDensity() + 0.5f).toInt()
}

fun Context.sp2px(sp: Int): Int {
    return (sp * getScreenDensity() + 0.5f).toInt()
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    if (imm != null && imm.isActive) {
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }
}
