package cn.mtjsoft.barcodescanning.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import cn.mtjsoft.barcodescanning.R

class ScanTypeAdapter(private val context: Context, private val datas: Array<String>) :
    PagerAdapter() {


    override fun getCount(): Int = datas.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val textView: TextView = View.inflate(context, R.layout.view_scan_type, null) as TextView
        textView.text = datas[position]
        container.addView(textView)
        return textView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return datas[position]
    }
}