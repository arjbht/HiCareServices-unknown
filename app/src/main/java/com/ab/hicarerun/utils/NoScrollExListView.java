package com.ab.hicarerun.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Created by Arjun Bhatt on 1/7/2020.
 */
public class NoScrollExListView extends ExpandableListView {

    public NoScrollExListView(Context context) {
        super(context);
    }
    public NoScrollExListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NoScrollExListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}