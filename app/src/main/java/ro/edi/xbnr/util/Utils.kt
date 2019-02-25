package ro.edi.xbnr.util

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter(value = ["src", "errorSrc", "placeholderSrc"], requireAll = false)
fun ImageView.setImageUrl(src: Int? = null, errorSrc: Int? = null, placeholderSrc: Int? = null) {
    val builder = Glide.with(context).load(src)

    with(builder) {
        errorSrc?.let {
            this.error(errorSrc)
        }
        placeholderSrc?.let {
            this.placeholder(placeholderSrc)
        }
        this.transition(DrawableTransitionOptions.withCrossFade(300))
        this.fitCenter()
    }

    builder.into(this)
}

fun getColorRes(context: Context, attrRes: Int): Int {
    val outValue = TypedValue()
    context.theme.resolveAttribute(attrRes, outValue, true)
    return outValue.resourceId
}