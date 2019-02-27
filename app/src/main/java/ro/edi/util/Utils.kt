/*
* Copyright 2019 Eduard Scarlat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.edi.util

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * @return Application's version name from `PackageManager`.
 */
fun getAppVersionName(context: Context): String {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName
}

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
        this.transition(DrawableTransitionOptions.withCrossFade(150))
        this.fitCenter()
    }

    builder.into(this)
}

fun getColorRes(context: Context, attrRes: Int): Int {
    val outValue = TypedValue()
    context.theme.resolveAttribute(attrRes, outValue, true)
    return outValue.resourceId
}