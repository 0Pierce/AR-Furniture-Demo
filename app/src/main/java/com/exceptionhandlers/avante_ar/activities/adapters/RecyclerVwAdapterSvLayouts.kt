package com.exceptionhandlers.avante_ar.activities.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.exceptionhandlers.avante_ar.R

class RecyclerVwAdapterSvLayouts(private var titles: List<String>) :
    RecyclerView.Adapter<RecyclerVwAdapterSvLayouts.ViewHolder>() {

inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


    val layoutTitle : TextView = itemView.findViewById<TextView>(R.id.tvTitle)

    init{
        itemView.setOnClickListener{
            val pos : Int = adapterPosition
            Toast.makeText(itemView.context, "Clicked on: "+ pos+1, Toast.LENGTH_SHORT).show()
        }
    }

}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_load_saved_layouts,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.layoutTitle.text = titles[position];
    }


}