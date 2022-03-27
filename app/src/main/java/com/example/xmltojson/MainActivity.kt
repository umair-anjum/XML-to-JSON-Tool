package com.example.xmltojson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.developer.kalert.KAlertDialog
import com.example.xmltojson.Util.dismissLoaderDialog
import com.example.xmltojson.Util.showErrorDialog
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private lateinit var sdcard: String
    private lateinit var loaderDialog: KAlertDialog

    private lateinit var start: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start = findViewById(R.id.start)

        start.setOnClickListener {
            if (PermissionHelper.isReadStorageAllowed(this)) {
                sdcard = Util.getRootPath(this)
                loaderDialog = Util.showLoaderDialog(this, "Converting Json")

                Handler(Looper.getMainLooper()).postDelayed({
                    dismissLoaderDialog(loaderDialog)
                }, 2000)
                //To read and convert XiB
                readXibFolder(sdcard + "/templates/")
                //Copy assets
                //  copyAssets(sdcard+"/templates/","SVG")
                //Copy Thumbnails
//            copyThumbnails(sdcard+"/thumbnails/",sdcard+"Converted/Thumbnails/")
            } else {
                PermissionHelper.requestStoragePermission(
                    this,
                    PermissionHelper.STORAGE_REQUEST_CODE
                )
            }
        }

    }

    private fun readXibFolder(path: String, startIndex: Int = 1) {
        val dir = File(path) //

        if (dir.exists()) {
            val totalNumFiles = dir.listFiles()!!.size
            val folders = dir.listFiles()!!

//            Arrays.sort(folders, object : Comparator<File?> {
//                override fun compare(o1: File?, o2: File?): Int {
//                    if (o1!!.lastModified() < o2!!.lastModified()) return 1
//                    return if (o1.lastModified() > o2.lastModified()) -1 else 0
//                }
//            })

            Log.e("totalNumFiles", "$totalNumFiles")
            Log.e("totalNumFiles", "${folders[0].name}")
            for (i in startIndex until totalNumFiles) {

                val folderName = folders[i].name
                Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")

                val pattern: Pattern = Pattern.compile(".*\\d.*")
                val matcher: Matcher = pattern.matcher(folderName)
                if (matcher.find()) {
//                    Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")

                    val file = File(path, "$folderName/$folderName.xib")
                    val inputStream: InputStream = FileInputStream(file)

                    val xmlToJson = XmlToJson.Builder(inputStream, null).build()
                    inputStream.close()
                    val formatted = xmlToJson.toFormattedString()

                    val mkConverted = File(sdcard + "Converted/")
                    val tempPath = File(sdcard + "Converted/Json/")
                    if (!mkConverted.exists()) {
                        mkConverted.mkdir()
                        if (!tempPath.exists()) {
                            tempPath.mkdir()
                        }
                    }
                    val filename = "$i.json"
                    val filePath = File(tempPath, filename)
                    Log.e("filePath", "$filePath")

                    val gsonString: String = formatted
                    println(gsonString)
                    write(gsonString, filePath)
                }

            }

            //Copy assets
            //  copyAssets(sdcard+"/templates/","SVG")
        } else {
            dismissLoaderDialog(loaderDialog)
            showErrorDialog(this, "Category Folder Not Found")
        }
//
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

                Log.e("totalNumAsset", "${files.size}")
                for (element in files) {
                    val fileName = element.name

                    try {
                        val fileSrc = File("$file/$fileName/")
                        val fileDest = File(sdcard + "Converted/Assets/")
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

                val file = File(path, "$folderName/")

                try {
                    val fileSrc = File("$file")
                    val fileDest = File(sdcard + "Converted/Thumbnails/")
                    if (!fileDest.exists()) {
                        fileDest.mkdir()
                    }
                    Log.e("fileSrc", "$fileSrc")
                    Log.e("fileSrc", "$fileDest")
                    copyFileOrDirectory(fileSrc.toString(), fileDest.toString())

                } catch (e: IOException) {
                    e.printStackTrace()
                }


//                val files = file.listFiles()!!

//                Log.e("totalNumAsset","${files.size}")
//                for (element in files){
//                    val fileName = element.name
//
//                }

                //Copy Thumbnails
//            copyThumbnails(sdcard+"/thumbnails/",sdcard+"Converted/Thumbnails/")
            }
        }

        val totalFiles = File(copiedPath).listFiles()!!.size
        val filesList = File(copiedPath).listFiles()!!
        for (j in startIndex until totalFiles) {
            val folderName = filesList[j].name
            rename(File("$copiedPath$folderName"), File(copiedPath + "($j).png"))
            Log.e("rename", "$copiedPath$folderName")
            Log.e("rename", "${(copiedPath + "($j).png")}")

        }
    }

    private fun rename(from: File, to: File): Boolean {
        return from.exists() && from.renameTo(to)
    }

    class WindowsExplorerComparator : Comparator<File?> {
        override fun compare(o1: File?, o2: File?): Int {
            val i1 = splitStringPreserveDelimiter(o1!!.name).iterator()
            val i2 = splitStringPreserveDelimiter(o2!!.name).iterator()
            while (true) {
                //Til here all is equal.
                if (!i1.hasNext() && !i2.hasNext()) {
                    return 0
                }
                //first has no more parts -> comes first
                if (!i1.hasNext() && i2.hasNext()) {
                    return -1
                }
                //first has more parts than i2 -> comes after
                if (i1.hasNext() && !i2.hasNext()) {
                    return 1
                }
                val data1 = i1.next()
                val data2 = i2.next()
                var result: Int
                try {
                    //If both datas are numbers, then compare numbers
                    result = java.lang.Long.compare(
                        java.lang.Long.valueOf(data1),
                        java.lang.Long.valueOf(data2)
                    )
                    //If numbers are equal than longer comes first
                    if (result == 0) {
                        result = -Integer.compare(data1.length, data2.length)
                    }
                } catch (ex: NumberFormatException) {
                    //compare text case insensitive
                    result = data1.compareTo(data2, ignoreCase = true)
                }
                if (result != 0) {
                    return result
                }
            }
        }

        private fun splitStringPreserveDelimiter(str: String): List<String> {
            val matcher = splitPattern.matcher(str)
            val list: MutableList<String> = ArrayList()
            var pos = 0
            while (matcher.find()) {
                list.add(str.substring(pos, matcher.start()))
                list.add(matcher.group())
                pos = matcher.end()
            }
            list.add(str.substring(pos))
            return list
        }

        companion object {
            private val splitPattern = Pattern.compile("\\d+|\\.|\\s")
        }
    }

}



