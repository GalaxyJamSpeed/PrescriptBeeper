package com.example.prescriptbeeper

import android.content.res.ColorStateList
import android.widget.Switch

object SwitchStyler {
    fun applyBlueTint(switch: Switch) {
        val thumbColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(0xFF4a90d9.toInt(), 0xFF5c6975.toInt())
        )
        val trackColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(0xFF2a5a8a.toInt(), 0xFF2a2f33.toInt())
        )
        switch.thumbTintList = thumbColors
        switch.trackTintList = trackColors
    }
}