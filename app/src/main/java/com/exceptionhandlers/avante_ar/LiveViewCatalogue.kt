package com.exceptionhandlers.avante_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.reflect.Modifier


class LiveViewCatalogue : Fragment(R.layout.fragment_live_view_catalogue) {
    fun Menu(modifer: Modifier){
        val itemsList = listOf(
            Furniture(name ="shelf", imageID = R.drawable.shelf, path = "drawable/shelf"),
            Furniture(name ="lamp", imageID = R.drawable.sofa, path = ""),
            Furniture(name ="bed", imageID = R.drawable.bed, path = ""),
            Furniture(name ="cabinet", imageID = R.drawable.cabinet, path = ""),
            Furniture(name ="mirror", imageID = R.drawable.mirror, path = ),

}

    data class Furniture(var name:String, var imageID:Int, var path: String)