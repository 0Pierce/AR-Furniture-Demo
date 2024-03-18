package com.exceptionhandlers.avante_ar

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import java.lang.reflect.Modifier
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


interface OnCatalogItemSelectedListener {
    fun onCatalogItemSelected(item: CatalogItem)

}

//Class to hold the items
data class CatalogItem(
    val name: String,
    val imgPath: Int,
//    val price: String,
//    val size: String
)

class LiveViewCatalogue : Fragment(R.layout.fragment_live_view_catalogue) {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        val view: View = inflater!!.inflate(R.layout.fragment_live_view_catalogue, container, false)


        // Return the fragment view/layout
        return view
    }
    //Checking when the fragment becomes visible
    private var listener: OnCatalogItemSelectedListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)





        var item : CatalogItem

        if (context is OnCatalogItemSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCatalogItemSelectedListener")
        }
    }




    override fun onDetach() {
        super.onDetach()
        listener = null
    }


//??????
    fun Menu(modifer: Modifier){
        var itemsList = listOf(
            Furniture(name ="shelf", imageID = R.drawable.shelf, path = "drawable/shelf"),
            Furniture(name ="sofa", imageID = R.drawable.sofa, path = "drawable/sofa"))
    }
}
// creates a clicker for each furniture
fun assigner(view: View, itemsList: List<Furniture>) {
    // Set listeners for each furniture instance using a loop
    for (furniture in itemsList) {
        var name = furniture.name
        val buttonId = view.resources.getIdentifier(name, "id", view.context.packageName)
        val button = view.findViewById<Button>(buttonId)
        button.setOnClickListener {
            //function(furniture.name)
        }
    }
}

    data class Furniture(var name:String, var imageID:Int, var path: String,
                         var listener: ((Furniture) -> Unit)? = null)