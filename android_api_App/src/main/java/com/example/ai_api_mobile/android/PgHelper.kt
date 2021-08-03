package com.example.ai_api_mobile.android

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PgHelper {
    companion object{
        fun sendApi(client: OkHttpClient,apikey:String,fileName:String,typeCard:String):JSONObject{
            if(typeCard.equals("front")){
                var query = "thai-national-id-card-front"
            }else if(typeCard.equals("back")){
                var query = "thai-national-id-card-back"
            }
            var data_format = JSONObject()
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            println(File(imagesDir,"1627380289812.jpg").toString())
            println(MultipartBody.FORM)
            val MEDIA_TYPE = "multipart/form-data; ".toMediaType()
            val postBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
//                .addFormDataPart("file","id-card-front.jpg", File(imagesDir,"x68btVc_d.jpg").asRequestBody("image/jpg".toMediaType()))
                .addFormDataPart("file","id-card-front.jpg", File(imagesDir,fileName).asRequestBody("image/jpg".toMediaType()))
                .build()
            val request = Request.Builder()
                .url("https://api.iapp.co.th/thai-national-id-card-front")
                .header("apikey",apikey)
                .post(postBody)
                .build()
            var checkRes = false
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    println(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if(!response.isSuccessful){
                        checkRes = true
                        throw IOException("Unexpected code $response")
                    }
//                println(response.body!!.string())
                    data_format = JSONObject(response.body!!.string())
//                    println("inside")
//                    println(data_format)

                }
            })
            while(data_format.length() == 0){
                println("waiting")
                if(checkRes){
                    break
                }
            }
            return data_format
        }
    }
}