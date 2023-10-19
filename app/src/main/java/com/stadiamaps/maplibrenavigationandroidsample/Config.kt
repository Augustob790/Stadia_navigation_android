package com.stadiamaps.maplibrenavigationandroidsample

import android.content.Context

object Config {
    const val STADIA_API_KEY = "YOUR-API-KEY"
}

// NOTE: This is not the cleanest, but it gets the job done and lets us specify
// map styles for everything in one place.
fun getStringAttributeFromStyle(context: Context, styleResId: Int, attrResId: Int): String? {
    val typedArray = context.theme.obtainStyledAttributes(styleResId, intArrayOf(attrResId))
    val value = typedArray.getString(0)
    typedArray.recycle()
    return value
}
