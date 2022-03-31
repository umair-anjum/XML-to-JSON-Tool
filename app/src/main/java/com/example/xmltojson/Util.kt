package com.example.xmltojson

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Environment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import java.io.File


object Util {

    const val THUMB_START_BRACE = "("
    const val THUMB_END_BRACE = ")"


    @Suppress("DEPRECATION")
    @JvmField
    val BASE_LOCAL_PATH = "${Environment.getExternalStorageDirectory().absolutePath}/XibTool/"

    @JvmStatic
    fun getRootPath(context: Context): String {
        var root: String? = null
        root = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir("XibTool")?.absolutePath + "/"
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

    fun showLoaderDialog(mContext:Context,title:String) : SweetAlertDialog{
        val pDialog = SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = title
        pDialog.setCancelable(false)
        pDialog.show()
        return pDialog
    }

    fun dismissLoaderDialog(pDialog:SweetAlertDialog){
        if(pDialog.isShowing){
            pDialog.dismiss()
        }
    }

    fun showErrorDialog(mContext:Context,title:String): SweetAlertDialog {
        val pDialog = SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
        pDialog.titleText = "Oops!!"
        pDialog.contentText = title
        pDialog.show()

        return pDialog
    }
    fun showErrorDialogEx(mContext:Context,title:String): SweetAlertDialog {
        val pDialog = SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
        pDialog.titleText = "Oops!!"
        pDialog.confirmText = "Ok"
//        pDialog.setCancelClickListener { sDialog ->
//            sDialog.dismissWithAnimation()
//        }
        pDialog.setConfirmClickListener { sDialog ->
            sDialog.dismissWithAnimation()
        }
        pDialog.contentText = title
        pDialog.show()

        return pDialog
    }

    fun warningDialog(mContext:Context,title:String){
        SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText("Won't be able to recover this file!")
            .setConfirmText("Yes,delete it!")
            .show()
    }

    fun successDialog(mContext:Context,title:String){
        SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Have Fun !!!!")
            .setContentText(title)
            .show()
    }
}