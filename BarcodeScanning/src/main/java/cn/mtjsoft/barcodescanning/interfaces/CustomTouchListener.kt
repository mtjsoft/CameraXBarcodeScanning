package cn.mtjsoft.barcodescanning.interfaces

/**
 * @author mtj
 * @date 2022/1/20
 * @desc 自定义触摸事件接口
 * @email mtjsoft3@gmail.com
 */
interface CustomTouchListener {
    /**
     * 放大
     */
    fun zoom()

    /**
     * 缩小
     */
    fun ZoomOut()

    /**
     * 单击
     */
    fun click(x: Float, y: Float)

    /**
     * 双击
     */
    fun doubleClick(x: Float, y: Float)

    /**
     * 长按
     */
    fun longClick(x: Float, y: Float)
}