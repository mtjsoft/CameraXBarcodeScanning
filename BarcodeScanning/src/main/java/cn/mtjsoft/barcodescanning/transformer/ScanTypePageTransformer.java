package cn.mtjsoft.barcodescanning.transformer;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class ScanTypePageTransformer implements ViewPager.PageTransformer {
    //初始
    private static final float MIN_SCALE = 0.85f;

    @Override
    public void transformPage(View view, float position) {
        float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
        if (position != 0) {
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setScaleX(1);
            view.setScaleY(1);
        }
    }
}
