package com.example.xmltojson

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object Util {

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

}