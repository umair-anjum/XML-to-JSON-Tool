package com.example.xmltojson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xmltojson.Util.dismissLoaderDialog
import com.example.xmltojson.Util.showErrorDialog
import com.example.xmltojson.Util.showErrorDialogEx
import com.google.android.material.textfield.TextInputLayout
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var sdcard: String
    private lateinit var loaderDialog: SweetAlertDialog

    private lateinit var start: Button
    private lateinit var categoryName: TextInputLayout
    private lateinit var startIndex: TextInputLayout
    private lateinit var assetsSpinner: Spinner
    private lateinit var thumbBrackets: CheckBox
    private lateinit var startJsonIndex: CheckBox
    private lateinit var reset: TextView

    private var mStartIndex = 0
    private var mCategoryName = ""
    private var mAssetsFolder = ""
    private var mThumbBrackets = false
    private var mStartJsonIndex = 0

    private var mFinalFolder = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //
        start = findViewById(R.id.start)
        categoryName = findViewById(R.id.categoryName)
        assetsSpinner = findViewById(R.id.assetsSpinner)
        startIndex = findViewById(R.id.startIndex)
        thumbBrackets = findViewById(R.id.thumbBrackets)
        startJsonIndex = findViewById(R.id.startJsonIndex)
        reset = findViewById(R.id.reset)
        //
        assetsSpinner.onItemSelectedListener = this
        sdcard = Util.getRootPath(this)
        //
        thumbBrackets.setOnCheckedChangeListener { _, isChecked ->
            mThumbBrackets = isChecked
        }
        //
        startJsonIndex.setOnCheckedChangeListener { _, isChecked ->
            mStartJsonIndex = if (isChecked) {
                1
            } else {
                0
            }
        }
        //
        reset.setOnClickListener {
            categoryName.editText?.setText("")
            startIndex.editText?.setText("")
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.assets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            assetsSpinner.adapter = adapter
        }
        //
        start.setOnClickListener {
            //
            if (PermissionHelper.isReadStorageAllowed(this)) {
                if (categoryName.editText?.text.toString() != "") {
                    mCategoryName = categoryName.editText?.text.toString()
                    if (startIndex.editText?.text.toString() != "") {
                        mStartIndex = startIndex.editText?.text.toString().toInt()
                        //
                        mFinalFolder = "$mCategoryName(Converted)"
                        val savingDir = File(mFinalFolder)
                        if(savingDir.exists()){
                            savingDir.delete()
                        }
                        //
                        loaderDialog = Util.showLoaderDialog(this, "Converting Json")

                        Handler(Looper.getMainLooper()).postDelayed({
//                            //To read and convert XiB
                            readXibFolder("$sdcard/$mCategoryName/templates/", mStartIndex)
                        }, 5000)

                    } else {
                        showErrorDialog(this, "Please Enter Start Index")
                    }
                } else {
                    showErrorDialog(this, "Please Enter Category Name")
                }

            } else {
                PermissionHelper.requestStoragePermission(
                    this,
                    PermissionHelper.STORAGE_REQUEST_CODE
                )
            }
        }

    }

    private var exception = false
    private fun readXibFolder(path: String, startIndex: Int = 1) {
        val dir = File(path) //
        if(dir.exists()){
        Log.e("dir", "$dir")
        if (dir.exists()) {
            val totalNumFiles = dir.listFiles()!!.size
            val folders = dir.listFiles()!!

//            val otherStrings =
//                arrayOf("LM_1", "LM_16", "LM_10", "LM_2", "LM_3", "LM_4", "LM_5", "LM_6")
            Arrays.sort(folders, Comparator { o1, o2 ->
                if (o1 == null) return@Comparator -1 else if (o2 == null) return@Comparator 1
                val cmp = o1.name.length.compareTo(o2.name.length)
                if (cmp != 0) cmp else o1.compareTo(o2)
            })
//            for (i in otherStrings.indices)
//                Log.e("otherStrings", otherStrings[i])

            Log.e("totalNumFiles", "$totalNumFiles")
            Log.e("totalNumFiles", folders[0].name)
            for (i in startIndex until totalNumFiles + 1) {

                val folderName = folders[i - 1].name
                Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")

                val pattern: Pattern = Pattern.compile(".*\\d.*")
                val matcher: Matcher = pattern.matcher(folderName)
                if (matcher.find()) {
//                    Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")
                    try {
                        val file = File(path, "$folderName/$folderName.xib")
                        if (file.exists()) {
                            val inputStream: InputStream = FileInputStream(file)

                            val xmlToJson = XmlToJson.Builder(inputStream, null).build()
                            inputStream.close()
                            val formatted = xmlToJson.toFormattedString()

                            val mkConverted = File("$sdcard$mFinalFolder/")
                            val tempPath = File("$sdcard$mFinalFolder/Json/")
                            if (!mkConverted.exists()) {
                                mkConverted.mkdir()
                                if (!tempPath.exists()) {
                                    tempPath.mkdir()
                                }
                            }
                            val filename = "${i - mStartJsonIndex}.json"
                            val filePath = File(tempPath, filename)
                            Log.e("filePath", "$filePath")

                            val gsonString: String = formatted
                            println(gsonString)
                            write(gsonString, filePath)
                        } else {
                            exception = true
                            Log.e("missing", "Xib no. ($folderName) is missing")
                            dismissLoaderDialog(loaderDialog)
                            showErrorDialogEx(this@MainActivity, "Xib no. ($folderName) is missing")
                        }
                    } catch (ex: Exception) {
                        exception = true
                        Log.e("xibEx", "Something went wrong..!!")
                        dismissLoaderDialog(loaderDialog)
                        showErrorDialog(this@MainActivity, "Something went wrong..!!")
                    }
                }

            }
            if (!exception) {
                //
                dismissLoaderDialog(loaderDialog)
                //Copy assets
                loaderDialog = Util.showLoaderDialog(this, "Coping Assets")

                Handler(Looper.getMainLooper()).postDelayed({
                    copyAssets("$sdcard/$mCategoryName/templates/", mAssetsFolder, mStartIndex)
                }, 5000)
            } else {
                dismissLoaderDialog(loaderDialog)
            }
        } else {
            dismissLoaderDialog(loaderDialog)
            showErrorDialog(this, "Category Folder Not Found")
        }
//
        }
        else {
            Log.e("missing", "Templates folder not found..!!")
            dismissLoaderDialog(loaderDialog)
            showErrorDialogEx(this@MainActivity, "Templates folder not found..!!")
        }
    }

    private fun write(data: String, path: File) {
        try {
            val output: Writer
            output = BufferedWriter(FileWriter(path))
            output.write(data)
            output.close()
            Log.e("files_status", "Composition saved")
        } catch (e: java.lang.Exception) {
            Log.e("files_status", "${e.message}")
        }
    }

    private fun copyAssets(path: String, assetsFolder: String, startIndex: Int = 1) {
        val dir = File(path) //
        if(dir.exists()){
        Log.e("assetsPath", "$dir")
        val totalNumFiles = dir.listFiles()!!.size
        val folders = dir.listFiles()!!

        Log.e("totalNumFiles", "$totalNumFiles")
        Log.e("totalNumFiles", folders[0].name)
        for (i in startIndex until totalNumFiles) {
            val folderName = folders[i].name
            val pattern: Pattern = Pattern.compile(".*\\d.*")
            val matcher: Matcher = pattern.matcher(folderName)
            if (matcher.find()) {
                Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")

                val file = File(path, "$folderName/$assetsFolder/")
                val files = file.listFiles()!!
                if (files.isNotEmpty()) {
                    Log.e("totalNumAsset", "${files.size}")
                    for (element in files) {
                        val fileName = element.name

                        try {
                            val fileSrc = File("$file/$fileName/")
                            val fileDest = File("$sdcard$mFinalFolder/Assets/")
                            if (!fileDest.exists()) {
                                fileDest.mkdir()
                            }
                            Log.e("fileSrc", "$fileSrc")
                            Log.e("fileSrc", "$fileDest")
                            copyFileOrDirectory(fileSrc.toString(), fileDest.toString())
                        } catch (e: IOException) {
                            exception = true
                            Log.e("xibEx", "Something went wrong..!!")
                            dismissLoaderDialog(loaderDialog)
                            showErrorDialog(this@MainActivity, "Something went wrong..!!")
                            e.printStackTrace()
                        }
                    }
                } else {
                    exception = true
                    Log.e("missing", "Assets missing in  ($folderName) folder")
                    dismissLoaderDialog(loaderDialog)
                    showErrorDialogEx(this@MainActivity, "Assets missing in  ($folderName) folder")
                }
            }
        }
        if (!exception) {
            dismissLoaderDialog(loaderDialog)
            loaderDialog = Util.showLoaderDialog(this, "Coping Thumbnails")

            copyThumbnails(
                "$sdcard/$mCategoryName/thumbnails/",
                "$sdcard$mFinalFolder/Thumbnails/",
                mStartIndex
            )
        }
        }
        else {
            Log.e("missing", "Templates folder not found..!!")
            dismissLoaderDialog(loaderDialog)
            showErrorDialogEx(this@MainActivity, "Templates folder not found..!!")
        }
    }

    private fun copyFileOrDirectory(srcDir: String?, dstDir: String?) {
        try {
            val src = File(srcDir!!)
            val dst = File(dstDir, src.name)
            if (src.isDirectory) {
                val files = src.list()
                val filesLength = files!!.size
                for (i in 0 until filesLength) {
                    val src1 = File(src, files[i]).path
                    val dst1 = dst.path
                    copyFileOrDirectory(src1, dst1)
                }
            } else {
                copyFile(src, dst)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File) {
        if (!destFile.parentFile!!.exists()) destFile.parentFile!!.mkdirs()
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        } finally {
            source?.close()
            destination?.close()
        }

    }

    private fun copyThumbnails(path: String, copiedPath: String, startIndex: Int = 0) {
        val dir = File(path) //
        if(dir.exists()) {
            val totalNumFiles = dir.listFiles()!!.size
            val folders = dir.listFiles()!!

            //
            Arrays.sort(folders, Comparator { o1, o2 ->
                if (o1 == null) return@Comparator -1 else if (o2 == null) return@Comparator 1
                val cmp = o1.name.length.compareTo(o2.name.length)
                if (cmp != 0) cmp else o1.compareTo(o2)
            })
            Log.e("totalNumFiles", "$totalNumFiles")
            Log.e("totalNumFiles", folders[0].name)
            for (i in startIndex until totalNumFiles + 1) {
                val folderName = folders[i - 1].name
                val pattern: Pattern = Pattern.compile(".*\\d.*")
                val matcher: Matcher = pattern.matcher(folderName)
                if (matcher.find()) {
                    Log.e("totalThumbnails", "file found ${i - 1}   ---  folderName:  $folderName")

                    val file = File(path, "$folderName/")

                    try {
                        val fileSrc = File("$file")
                        val fileDest = File("$sdcard$mFinalFolder/Thumbnails/")
                        if (!fileDest.exists()) {
                            fileDest.mkdir()
                        }
                        Log.e("fileSrc", "$fileSrc")
                        Log.e("fileSrc", "$fileDest")
                        copyFileOrDirectory(fileSrc.toString(), fileDest.toString())

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }


            Handler(Looper.getMainLooper()).postDelayed({
                val totalFiles = File(copiedPath).listFiles()!!.size
                val filesList = File(copiedPath).listFiles()!!
                //
                Arrays.sort(filesList, Comparator { o1, o2 ->
                    if (o1 == null) return@Comparator -1 else if (o2 == null) return@Comparator 1
                    val cmp = o1.name.length.compareTo(o2.name.length)
                    if (cmp != 0) cmp else o1.compareTo(o2)
                })
                //
                for (j in startIndex until totalFiles + 1) {
                    val folderName = filesList[j - 1].name
                    if (mThumbBrackets)
                        rename(File("$copiedPath$folderName"), File(copiedPath + "(${j}).png"))
                    else
                        rename(File("$copiedPath$folderName"), File(copiedPath + "${j}.png"))

                    Log.e("rename", "$copiedPath$folderName")
//                Log.e("rename", "${(copiedPath + "($j).png")}")

                }
                dismissLoaderDialog(loaderDialog)
                Util.successDialog(this@MainActivity, "Converted Successfully")
            }, 5000)
        }
        else {
            Log.e("missing", "Thumbs folder not found..!!")
            dismissLoaderDialog(loaderDialog)
            showErrorDialogEx(this@MainActivity, "Thumbs folder not found..!!")
        }
    }

    private fun rename(from: File, to: File): Boolean {
        return from.exists() && from.renameTo(to)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position).toString()
        mAssetsFolder = selectedItem
        Log.e("mAssetsFolder", "mAssetsFolder: $mAssetsFolder")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.e("mAssetsFolder", "onNothingSelected: $mAssetsFolder")
    }

}



