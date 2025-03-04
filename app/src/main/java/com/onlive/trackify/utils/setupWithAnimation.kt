package com.onlive.trackify.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.onlive.trackify.R

fun BottomNavigationView.setupWithAnimation() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.bottom_nav_item_animation)

    setOnItemSelectedListener { item ->
        val view = findViewById<View>(item.itemId)
        view?.startAnimation(animation)

        val container = parent as? View
        if (container != null) {
            val scaleX = ObjectAnimator.ofFloat(container, View.SCALE_X, 0.95f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(container, View.SCALE_Y, 0.95f, 1.0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        true
    }
}