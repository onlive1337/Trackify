package com.onlive.trackify.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.onlive.trackify.R

class HelperScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    init {
        clipToPadding = false
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + getCustomNavigationBarHeight())
    }

    private fun getCustomNavigationBarHeight(): Int {
        return resources.getDimensionPixelSize(R.dimen.bottom_nav_height) +
                resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
    }
}

class HelperNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    init {
        clipToPadding = false
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + getCustomNavigationBarHeight())
    }

    private fun getCustomNavigationBarHeight(): Int {
        return resources.getDimensionPixelSize(R.dimen.bottom_nav_height) +
                resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
    }
}