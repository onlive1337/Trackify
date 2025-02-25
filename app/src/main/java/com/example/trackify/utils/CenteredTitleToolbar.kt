package com.example.trackify.utils

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children

class CenteredTitleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.toolbarStyle
) : Toolbar(context, attrs, defStyleAttr) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        children.forEach { child ->
            if (child.javaClass.name.contains("Title")) {
                val params = child.layoutParams as LayoutParams
                params.gravity = Gravity.CENTER
                params.width = LayoutParams.MATCH_PARENT
                child.layoutParams = params
            }
        }
    }
}