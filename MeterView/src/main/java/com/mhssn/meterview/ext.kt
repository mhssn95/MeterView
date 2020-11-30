package com.mhssn.meterview

import android.content.Context

fun Int.toDp(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun Int.getColor(context: Context): Int {
    return context.resources.getColor(this, null)
}

fun Float.format(): String {
    return "%.2f".format(this*100).plus("%")
}