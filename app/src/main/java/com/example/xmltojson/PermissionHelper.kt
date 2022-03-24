package com.example.xmltojson

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.facebook.internal.Validate


object PermissionHelper {

    const val STORAGE_REQUEST_CODE = 11
    const val TEMPLATE_REQUEST_CODE = 12
    private var requestCode = 1

    @JvmStatic
    fun isReadStorageAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return hasMediaLocationPermission(context)
        } else {
        //Getting the permission status
        val result =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        //If permission is granted returning true
        return result == PackageManager.PERMISSION_GRANTED
        //If permission is not granted returning false
          }

    }

    @JvmStatic
    fun requestStoragePermission(activity: Activity, requestCode: Int) {
        if (isReadStorageAllowed(activity)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionHelper.requestCode = requestCode
            requestMediaLocationPermission(activity, requestCode)
        } else {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        PermissionHelper.requestCode = requestCode
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestCode
        )
        }
    }

    @JvmStatic
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        callback: PermissionCallBacks,
    ) {

        //Checking the request code of our request
        if (requestCode == PermissionHelper.requestCode) {

            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                //  Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
                callback.onPermission(requestCode,true)
            } else {
                //Displaying another toast if permission is not granted
                callback.onPermission(requestCode,false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestMediaLocationPermission(context: Activity, requestCode: Int) {
        requestPermissions(
            context, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
            ), requestCode
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasMediaLocationPermission(context: Context): Boolean {
        return Validate.hasPermission(
            context,
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )

    }
}


interface PermissionCallBacks {
    fun onPermission(requestCode: Int, granted: Boolean)
}