package com.sanjarbek.mlkitfunproject

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(val context: Context, val productModelArrayList: ArrayList<ProductModel>): RecyclerView.Adapter<ProductAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = productModelArrayList.get(position)
        Log.d("TAG", "onBindViewHolder: ${model.get_name()}")
        holder.name.text = model.get_name()
        holder.date.text = model.get_date()
        holder.id.text = model.get_id()
    }

    override fun getItemCount(): Int {
        return productModelArrayList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var name = itemView.findViewById<TextView>(R.id.name)
        var id = itemView.findViewById<TextView>(R.id.id)
        var date = itemView.findViewById<TextView>(R.id.date)
    }
}