package com.example.xmltojson

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Environment
import com.developer.kalert.KAlertDialog
import java.io.File


object Util {

    const val THUMB_START_BRACE = "("
    const val THUMB_END_BRACE = ")"


    @Suppress("DEPRECATION")
    @JvmField
    val BASE_LOCAL_PATH = "${Environment.getExternalStorageDirectory().absolutePath}/JsonConverter/"

    @JvmStatic
    fun getRootPath(context: Context): String {
        var root: String? = null
        root = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir("JsonConverter")?.absolutePath + "/"
        } else {
            BASE_LOCAL_PATH
        }

        if (root != null) {
            val dirDest = File(root)
            if (!dirDest.exists()) {
                dirDest.mkdirs()
            }
        }

        return root.toString()
        //  return BASE_LOCAL_PATH
    }

    fun showLoaderDialog(mContext:Context,title:String) : KAlertDialog{
        val pDialog = KAlertDialog(mContext, KAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = title
        pDialog.setCancelable(false)
        pDialog.show()
        return pDialog
    }

    fun dismissLoaderDialog(pDialog:KAlertDialog){
        if(pDialog.isShowing){
            pDialog.dismiss()
        }
    }

    fun showErrorDialog(mContext:Context,title:String):KAlertDialog{
        val pDialog = KAlertDialog(mContext, KAlertDialog.ERROR_TYPE)
        pDialog.titleText = "Oops..."
        pDialog.contentText = title
        pDialog  .show()

        return pDialog
    }
}