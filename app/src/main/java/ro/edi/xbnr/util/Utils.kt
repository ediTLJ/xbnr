package ro.edi.xbnr.util

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter(value = ["src", "errorSrc", "placeholderSrc"], requireAll = false)
fun ImageView.setImageUrl(src: Int? = null, errorSrc: Int? = null, placeholderSrc: Int? = null) {
    val builder = Glide.with(context).load(src)

    errorSrc?.let {
        builder.error(errorSrc)
    }
    placeholderSrc?.let {
        builder.placeholder(placeholderSrc)
    }

    builder.fitCenter().into(this)
}

fun getColorRes(context: Context, attrRes: Int): Int {
    val outValue = TypedValue()
    context.theme.resolveAttribute(attrRes, outValue, true)
    return outValue.resourceId
}