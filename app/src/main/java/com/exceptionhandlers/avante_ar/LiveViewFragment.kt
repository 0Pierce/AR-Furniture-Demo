package com.exceptionhandlers.avante_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.filament.Viewport
import io.github.sceneview.ar.ARSceneView


/*
*Name: LiveViewFragment
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* Holds the AR viewscene, most AR logic involving detection and so on will be created here
* TBD
*
*
*
*
*
* ========Primary Functions:========
*Sceneview viewport inside XML
* TBD
*
*

* */
class LiveViewFragment : Fragment(R.layout.fragment_live_view) {

    lateinit var viewport : ARSceneView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}