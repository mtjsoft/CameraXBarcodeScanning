package cn.mtjsoft.barcodescanning.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.SoundPool
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import cn.mtjsoft.barcodescanning.R
import java.util.*

/**
 * @author mtj
 * @date 2022/1/21
 * @desc 扫码提示语播放
 * @email mtjsoft3@gmail.com
 */
class SoundPoolUtil private constructor() {
    private var soundPool: SoundPool? = null
    private var soundMap: HashMap<Int, Int>? = null

    private var vibrator: Vibrator? = null

    companion object {
        val instance: SoundPoolUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SoundPoolUtil()
        }
    }

    /**
     * 加载扫码提示语
     */
    fun loadQrcodeCompletedWav(context: Context) {
        if (soundPool == null) {
            soundMap = HashMap(2)
            soundPool = SoundPool.Builder().setMaxStreams(1).build()
            soundPool?.let {
                soundMap!![1] = it.load(context, R.raw.qrcode_completed_2, 1)
            }
        }
        if (vibrator == null) {
            vibrator = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * 播放扫码声音
     */
    fun playQrcodeCompleted() {
        soundMap?.let {
            val soundID = it[1] ?: -1
            if (soundID != -1) {
                soundPool?.play(soundID, 1f, 1f, 1, 0, 1f)
            }
        }
        startVibrator()
    }

    private fun startVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE))
            } else {
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}