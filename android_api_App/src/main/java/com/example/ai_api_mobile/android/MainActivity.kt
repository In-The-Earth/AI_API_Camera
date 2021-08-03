package com.example.ai_api_mobile.android

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import com.example.ai_api_mobile.Greeting
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import org.json.JSONObject
import toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

fun greet(): String {
    return Greeting().greeting()
}

private const val FILE_NAME = "photo.jpg"
private lateinit var photoFile: File
private lateinit var bm: Bitmap
private var check: Boolean = false
private lateinit var file_image: File
private val client = OkHttpClient()
private var file_name = ""
private var jsonResults = JSONObject()
private val MAX_IMAGE_SIZE = 700 * 1024

class MainActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        warning.text = ""
        warning2.text = ""

        val btn_camera1: Button = findViewById(R.id.btn_camera)
        btn_camera1.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider = FileProvider.getUriForFile(this,"com.example.ai_api_mobile.fileprovider",
                photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ){
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

//        val tv: TextView = findViewById(R.id.text_view)
//        tv.text = greet()
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  File.createTempFile(fileName,"jpg", storageDirectory)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] ==PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }else{
                Toast.makeText(
                    this,
                    "you deny permission for camera!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE) {
                val thumbNail = BitmapFactory.decodeFile(photoFile.absolutePath)
                val iv_image = findViewById<AppCompatImageView>(R.id.iv_image)
                iv_image.setImageBitmap(thumbNail)
                bm = thumbNail
                check = true
                println("done!!")
            }else{
                println("can't set image!!")
            }
        }
    }


    fun submit_btn(view: View){
        warning.text = ""
        warning2.text = ""
        if(check) {
            val resizeBitmap = bm.scale(500)
            saveMediaToStorage(resizeBitmap)
//            saveMediaToStorage(scaleDown(bm, MAX_IMAGE_SIZE.toFloat(),true))
            println("save done")
            toast("Sending AI API")
            val dataJson = PgHelper.sendApi(client,"Your api key", file_name,"front")
            jsonResults = dataJson
            println(dataJson)
            println("api done")
            if(jsonResults.length() == 0){
                warning2.text = "Card not found!! please try again"
            }else {
//                println(jsonResults.get("address"))
                val intent = Intent(this, MainActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("jsonResponse", jsonResults.toString())
                startActivity(intent)
            }
        }else{
            warning.text = "Please Take camera first"
        }

        //new scene
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        file_name = filename
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            file_image = image
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            toast("Saved to Photos")
        }
    }

//    private fun scaleDown(realImage:Bitmap, maxImageSize: Float, filter:Boolean): Bitmap {
//        val ratio:Float = Math.min(
//            maxImageSize/realImage.width,
//            maxImageSize/realImage.height
//        )
//        val width = (ratio * realImage.width).roundToInt()
//        val height = (ratio * realImage.height).roundToInt()
//
//        val newBitmap = Bitmap.createScaledBitmap(realImage,width,height,filter)
//        return newBitmap
//    }

}
fun Bitmap.scale(maxWidthAndHeight:Int):Bitmap{
    var newWidth = 0
    var newHeight = 0

    if (this.width >= this.height){
        val ratio:Float = this.width.toFloat() / this.height.toFloat()

        newWidth = maxWidthAndHeight
        // Calculate the new height for the scaled bitmap
        newHeight = Math.round(maxWidthAndHeight / ratio)
    }else{
        val ratio:Float = this.height.toFloat() / this.width.toFloat()

        // Calculate the new width for the scaled bitmap
        newWidth = Math.round(maxWidthAndHeight / ratio)
        newHeight = maxWidthAndHeight
    }

    return Bitmap.createScaledBitmap(
        this,
        newWidth,
        newHeight,
        false
    )
}
