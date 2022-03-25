package com.example.xmltojson

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.xmltojson.Util.THUMB_END_BRACE
import com.example.xmltojson.Util.THUMB_START_BRACE
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private lateinit var sdcard:String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(PermissionHelper.isReadStorageAllowed(this)) {
             sdcard = Util.getRootPath(this)

//            val assetManager: AssetManager = assets
//            val inputStream: InputStream = assetManager.open("myFile.xml")

//            val file = File(sdcard, "newFile.xib")
//            val inputStream: InputStream = FileInputStream(file)
//
//            val xmlToJson = XmlToJson.Builder(inputStream, null).build()
//            inputStream.close()
//            val formatted = xmlToJson.toFormattedString()
//
//            val data = convertXmlToJson(
//                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
//                        "<library>\n" +
//                        "    <book id=\"007\">James Bond</book>\n" +
//                        "    <book id=\"000\">Book for the dummies</book>\n" +
//                        "</library>"
//            )
//            Log.e("result", "$formatted")
//
//
//            val filename = "trending_tag" + ".json"
//            val filePath = File(sdcard, filename)
////        val gsonString: String = Gson().toJson(model)
//            val gsonString: String = formatted
//            println(gsonString)
//            write(gsonString, filePath)
            readXibFolder(sdcard+"/templates/")
        }
        else{
            PermissionHelper.requestStoragePermission(this, PermissionHelper.STORAGE_REQUEST_CODE)
        }
    }
    private fun readXibFolder(path:String,startIndex:Int = 1){
        val dir = File(path) //

        val totalNumFiles = dir.listFiles()!!.size
        val folders = dir.listFiles()!!
        Log.e("totalNumFiles","$totalNumFiles")
        Log.e("totalNumFiles","${folders[0].name}")
        for (i in startIndex until totalNumFiles) {
            val folderName = folders[i].name
            val pattern: Pattern = Pattern.compile(".*\\d.*")
            val matcher: Matcher = pattern.matcher(folderName)
            if (matcher.find()) {
                Log.e("totalNumFiles", "file found $i  ---  folderName:  $folderName")

                val file = File(path, "$folderName/$folderName.xib")
                val inputStream: InputStream = FileInputStream(file)

                val xmlToJson = XmlToJson.Builder(inputStream, null).build()
                inputStream.close()
                val formatted = xmlToJson.toFormattedString()

                val tempPath = File(sdcard+"Converted/")
                if(!tempPath.exists()){
                    tempPath.mkdir()
                }
                val filename = "$i.json"
                val filePath = File(tempPath, filename)
                Log.e("filePath","$tempPath")

                val gsonString: String = formatted
                println(gsonString)
                write(gsonString, filePath)
            }
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

}



