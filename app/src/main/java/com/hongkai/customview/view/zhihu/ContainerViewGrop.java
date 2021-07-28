package com.hongkai.customview.view.zhihu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ContainerViewGrop extends ViewGroup {

    public ContainerViewGrop(Context context) {
        this(context, null);
    }

    public ContainerViewGrop(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContainerViewGrop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        final int count = getVirtualChildCount();
        int height = 0;
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            height += child.getMeasuredHeight();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutVertical(l, t, r, b);
    }


    private int getVirtualChildCount() {
        return getChildCount();
    }

    @androidx.annotation.Nullable
    View getVirtualChildAt(int index) {
        return getChildAt(index);
    }


    void layoutVertical(int left, int top, int right, int bottom) {
        final int count = getVirtualChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt(i);
            int height = child.getMeasuredHeight();
            child.layout(left, top, right, top + height);
            top += height;
        }
    }


}
