package com.exceptionhandlers.avante_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.reflect.Modifier


class LiveViewCatalogue : Fragment(R.layout.fragment_live_view_catalogue) {

}


// Create New assets folder for assets + add Android Resource Directory (models)
// Paste assets into the folder (need to get our assets) (shelf, lamp, bed, cabinet, etc)
// Drawable files with the models too (ex.. burger.webp, shelf.webp, lamp.webp)

@Composable
fun Menu(modifer: Modifier){
    val itemsList = listOf(
        Furniture(name ="shelf", imageID = R.drawable.shelf, path = "drawable/shelf"),
        Furniture(name ="lamp", imageID = R.drawable.sofa, path = ""),
        Furniture(name ="bed", imageID = R.drawable.bed, path = ""),
        Furniture(name ="cabinet", imageID = R.drawable.cabinet, path = ""),
        Furniture(name ="mirror", imageID = R.drawable.mirror, path = ),
    )
    Row(modifier = modifier.fillMaxWidth(),
        verticalAlightment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
        ) { this:Rowscope
            IconButton(onClick = {/*TODO*/}) {
                // Need to add android icon buttons in a bit for the back in front arrows
                Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24), contentDescription = "previous")
            }


        IconButton(onClick = {/*TODO*/}) {
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24, contentDescription = "next")
        }


    }
}


data class Furniture(var name:String, var imageID:Int, var path: String)