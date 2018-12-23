package com.trainerapp.ui.customui

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

import com.trainerapp.R

class MaxHeightScrollView : ScrollView {
    private var maxHeight: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
            maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, 200) //200 is a defualt value
            styledAttrs.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
