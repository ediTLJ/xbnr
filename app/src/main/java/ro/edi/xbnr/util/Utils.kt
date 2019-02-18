package ro.edi.xbnr.util

import android.content.Context
import android.util.TypedValue

fun getColorRes(context: Context, attrRes: Int): Int {
    val outValue = TypedValue()
    context.theme.resolveAttribute(attrRes, outValue, true)
    return outValue.resourceId
}