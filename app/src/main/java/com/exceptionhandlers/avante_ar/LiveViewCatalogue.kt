package com.exceptionhandlers.avante_ar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.reflect.Modifier


class LiveViewCatalogue : Fragment(R.layout.fragment_live_view_catalogue) {

}

// Need to add drawable items
// Add item set for arrows once I get back home
// Create New assets folder for assets + add Android Resource Directory (models)
// Paste assets into the folder (need to get our assets) (shelf, lamp, bed, cabinet, etc)
// Drawable files with the models too (ex.. burger.webp, shelf.webp, lamp.webp)

@Composable
fun Menu(modifer: Modifier){
    val itemsList = listOf(
        Furniture(name ="shelf", imageID = R.drawable.shelf, path = "drawable/shelf"),
        Furniture(name ="lamp", imageID = R.drawable.lamp, path = ""),
        Furniture(name ="bed", imageID = R.drawable.bed, path = ""),
        Furniture(name ="cabinet", imageID = R.drawable.cabinet, path = ""),
        Furniture(name ="mirror", imageID = R.drawable.mirror, path = ),

    )
    Row(modifier = modifier.fillMaxWidth(),
        verticalAlightment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
        ) { this:Rowscope
            IconButton(onClick = {/*TODO*/}) {
                Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24))
            }

        }
    }


data class Furniture(var name:String, var imageID:Int, var path: String)