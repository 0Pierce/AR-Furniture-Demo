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
    fun onCatalogItemSelected(item: CatalogItems)

}

//Class to hold the items
data class CatalogItems(
    val name: String,
    val imgPath: String
//    val price: String,
//    val size: String
)

class LiveViewCatalogue : Fragment(R.layout.fragment_live_view_catalogue) {
    lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        view = inflater!!.inflate(R.layout.fragment_live_view_catalogue, container, false)


        // Return the fragment view/layout
        return view
    }

    //Checking when the fragment becomes visible
    private var listener: OnCatalogItemSelectedListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)




        Menu()


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
    fun Menu() {
        var itemsList = listOf(
            CatalogItems(name = "shelf", imgPath = "drawable/shelf"),
            CatalogItems(name = "sofa", imgPath = "drawable/sofa")
        )

        for (furniture in itemsList) {

            var name = furniture.name
            val buttonId = view.resources.getIdentifier(name, "id", view.context.packageName)
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener {
                listener?.onCatalogItemSelected(furniture)
            }
        }
    }








}