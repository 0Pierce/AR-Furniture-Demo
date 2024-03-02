package com.exceptionhandlers.avante_ar

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView

/*
*Name: LiveViewActivity
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* The primary AR Activity which will handle AR functionality/capabilities
* All AR related classes/APIs will culminate here
* TBD
*
*
*
*
* ========Primary Functions:========
*Hold LiveView fragment which creates a AR viewport
* TBD
*
*

* */
class LiveViewActivity : AppCompatActivity() {


    lateinit var catalogue: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_view)

        val btnBack = findViewById<Button>(R.id.btnBack)

        //Button to go back to Homepage
        btnBack.setOnClickListener{
            startActivity(Intent(this, HomePageActivity::class.java))

        }

        catalogue=findViewById(R.id.catalogueFragment)
        val btnCatOpen = findViewById<ImageButton>(R.id.imgBtnCatOpen)
        val btnCatClose = findViewById<ImageButton>(R.id.imgBtnCatClose)
        catalogue.isVisible = false



        btnCatOpen.setOnClickListener{
            catalogue.isVisible = true
            btnCatClose.isVisible = true
        }

        btnCatClose.setOnClickListener{
            catalogue.isVisible = false
            btnCatClose.isVisible = false
        }

    }
}