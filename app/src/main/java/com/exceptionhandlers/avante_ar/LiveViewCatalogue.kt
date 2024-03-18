package com.exceptionhandlers.avante_ar

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast


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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view: View
        view = inflater!!.inflate(R.layout.fragment_live_view_catalogue, container, false)

        menu(view)
        // Return the fragment view/layout
        return view
    }

    //Checking when the fragment becomes visible
    private var listener: OnCatalogItemSelectedListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)

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
    fun menu(view: View) {
        var itemsList = listOf(
            CatalogItems(name = "shelf", imgPath = "drawable/shelf"),
            CatalogItems(name = "sofa", imgPath = "drawable/sofa")
        )
        var i : Int = 0
//        for (furniture in itemsList) {
//            i+=1
//            var name = furniture.name
//            val buttonId = view.resources.getIdentifier(name)
//            val button = view.findViewById<Button>(buttonId)
//            button.setOnClickListener {
//                if(i <= 1){
//                    listener?.onCatalogItemSelected(furniture)
//                }else{
//                    Toast.makeText(context, "No Model added", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//            i+=1
//        }
    }








}