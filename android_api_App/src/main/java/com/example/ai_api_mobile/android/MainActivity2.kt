package com.example.ai_api_mobile.android

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import kotlinx.android.synthetic.main.activity_main2.*
import org.json.JSONObject
import java.util.*

private var jsonObj = JSONObject()
private var th_name = "TH NAME : "
private var en_name = "EN_NAME : "
private var id = "ID : "
private var dob = ""
private var address = ""
private var dois = ""
private var doe = ""

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val intent: Intent = getIntent()
        jsonObj = JSONObject(getIntent().getStringExtra("jsonResponse"))
//        println(jsonObj.get("id_number"))

        val imageBytes = Base64.decode(jsonObj.get("face").toString(),Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.size)
        img_profile.setImageBitmap(decodedImage)

        txt_dob.text = ""
        txt_address.text = ""
        txt_dois.text = ""
        txt_doe.text = ""

        th_name += ""+jsonObj.get("th_init") +" "+ jsonObj.get("th_fname") +" "+ jsonObj.get("th_lname")
        txt_name.text = th_name
        txt_name.textSize = 15F

        en_name += ""+jsonObj.get("en_init") +" "+ jsonObj.get("en_fname") +" "+ jsonObj.get("en_lname")
        txt_nameEn.text = en_name
        txt_nameEn.textSize = 15F

        id += jsonObj.get("id_number").toString()
        txt_id.text = id
        txt_id.textSize = 15F

        dob = jsonObj.get("th_dob").toString() + " | " + jsonObj.get("en_dob")
        address = jsonObj.get("address").toString()
        dois = jsonObj.get("th_issue").toString() + " | " + jsonObj.get("en_issue")
        doe = jsonObj.get("th_expire").toString() + " | " + jsonObj.get("en_expire")
    }

    fun showmore_btn(view: View){
        txt_dob.text = ""
        txt_address.text = ""
        txt_dois.text = ""
        txt_doe.text = ""
        if(cb_dob.isChecked){
            txt_dob.text = dob
        }
        if(cb_address.isChecked){
            txt_address.text = address
        }
        if(cb_doIssue.isChecked){
            txt_dois.text = dois
        }
        if(cb_DOExpiry.isChecked){
            txt_doe.text = doe
        }
    }
}