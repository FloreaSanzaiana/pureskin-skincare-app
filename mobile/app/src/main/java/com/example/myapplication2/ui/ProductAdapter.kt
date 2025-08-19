package com.example.myapplication2.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.util.ImageSetter

class ProductAdapter( private val context: Context,private var productList: MutableList<Product>,private var routine_name:String) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.productName)
        val type: TextView = view.findViewById(R.id.productType)
        val image: ImageView = view.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.name
        holder.type.text = product.type
        val imageResId = ImageSetter.getImageResourceId(product.area, product.type, context)
        holder.image.setImageResource(imageResId)
        holder.itemView.setOnClickListener {

            val fragment = ProductDetailsBottomSheetFragment(product)
            val bundle = Bundle()
            bundle.putString("routine_name", routine_name)
            fragment.arguments = bundle

            fragment.show((context as AppCompatActivity).supportFragmentManager, fragment.tag)
        }

    }

    fun setProducts(newProducts: List<Product>) {
        productList.clear()
        productList.addAll(newProducts)
        notifyDataSetChanged()
    }
}

