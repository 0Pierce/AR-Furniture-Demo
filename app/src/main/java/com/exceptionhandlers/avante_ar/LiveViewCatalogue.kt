package com.exceptionhandlers.avante_ar
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.exceptionhandlers.avante_ar.LiveViewActivity




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
    private val selectedItem: SelectedItem = SelectedItem("random", "random")
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
        Toast.makeText(context, "Attached", Toast.LENGTH_SHORT).show()
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



    fun coool(){

    }

    fun menu(view: View) {
        var itemsList = listOf(
            CatalogItems(name = "shelf", imgPath = "drawable/shelf"),
            CatalogItems(name = "sofa", imgPath = "drawable/sofa")
        )
        val size = itemsList.size
        for (furniture in itemsList) {
            var name = furniture.name
            val buttonId = view.resources.getIdentifier(name, "id", context?.packageName)
            val button = view.findViewById<LinearLayout>(buttonId)

            button?.setOnClickListener {
                if (size <= 2) {
                    updateSelected(furniture)
                    listener?.onCatalogItemSelected(furniture)
                } else {
                    Toast.makeText(context, "No Model added", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun updateSelected(furniture: CatalogItems) {
        // Update the values of selectedItem
        selectedItem.apply {
            name = furniture.name
            path = furniture.imgPath
        }


    }

    data class SelectedItem(var name: String, var path: String) {
        companion object {
            // Singleton object to hold the selected item
            private var instance: SelectedItem? = null

            fun getInstance(): SelectedItem {
                if (instance == null) {
                    instance = SelectedItem("random", "random")
                }
                return instance!!
            }
        }
    }
}