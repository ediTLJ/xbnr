//package ro.edi.util
//
//import android.content.Context
//import android.util.Log
//import com.bumptech.glide.GlideBuilder
//import com.bumptech.glide.annotation.GlideModule
//import com.bumptech.glide.module.AppGlideModule
//import ro.edi.xbnr.BuildConfig
//
//@GlideModule
//class MyAppGlideModule : AppGlideModule() {
//    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        builder.setLogLevel(if (BuildConfig.DEBUG) Log.WARN else Log.ERROR)
//    }
//}