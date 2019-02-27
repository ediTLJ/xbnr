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

import ro.edi.xbnr.BuildConfig

fun logd(tag: String, vararg txt: Any?) {
    if (!BuildConfig.DEBUG) return

    val count = txt.size

    if (count == 1) {
        android.util.Log.d(tag, txt[0].toString())
    } else { // count > 1
        val sb = StringBuilder(50 * count)

        for (aTxt in txt) {
            sb.append(aTxt)
        }

        android.util.Log.d(tag, sb.toString())
    }
}

fun logi(tag: String, vararg txt: Any?) {
    if (!BuildConfig.DEBUG) return

    val count = txt.size

    if (count == 1) {
        android.util.Log.i(tag, txt[0].toString())
    } else { // count > 1
        val sb = StringBuilder(50 * count)

        for (aTxt in txt) {
            sb.append(aTxt)
        }

        android.util.Log.i(tag, sb.toString())
    }
}

/**
 * @param txt List of texts to append.
 */
fun logw(tag: String, vararg txt: Any?) {
    if (!BuildConfig.DEBUG) return

    val count = txt.size

    if (count == 1) {
        android.util.Log.w(tag, txt[0].toString())
    } else { // count > 1
        val sb = StringBuilder(50 * count)

        for (aTxt in txt) {
            sb.append(aTxt)
        }

        android.util.Log.w(tag, sb.toString())
    }
}

/**
 * @param txt List of texts to append.
 */
fun loge(tag: String, vararg txt: Any?) {
    if (!BuildConfig.DEBUG) return

    val count = txt.size

    if (count == 1) {
        android.util.Log.e(tag, txt[0].toString())
    } else { // count > 1
        val sb = StringBuilder(50 * count)

        for (aTxt in txt) {
            sb.append(aTxt)
        }

        android.util.Log.e(tag, sb.toString())
    }
}

/**
 * Prints out exception stack traces.<br></br>
 */
fun printStackTrace(tag: String, e: Throwable?) {
    if (!BuildConfig.DEBUG) return

    if (e == null) {
        android.util.Log.e(tag, "Null exception. Hmm...")
        return
    }

    android.util.Log.e(tag, android.util.Log.getStackTraceString(e))
    // e.printStackTrace();
}