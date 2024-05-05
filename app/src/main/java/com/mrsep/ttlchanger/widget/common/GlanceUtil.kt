package com.mrsep.ttlchanger.widget.common

import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.background
import com.mrsep.ttlchanger.R

fun GlanceModifier.backgroundCompat(
    backgroundOpacity: Int
): GlanceModifier {
    return when (backgroundOpacity) {
        in 0..25 -> this
        in 25..75 -> background(ImageProvider(R.drawable.widget_shape_translucent))
        else -> background(ImageProvider(R.drawable.widget_shape))
    }
}