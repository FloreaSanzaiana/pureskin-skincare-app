package com.example.myapplication2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.ProductRecommended
import com.example.myapplication2.util.ImageSetter

class ProductResultedAdapter(
    private val productList: List<ProductRecommended>,
    private val onProductClick: (ProductRecommended) -> Unit
) : RecyclerView.Adapter<ProductResultedAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productType: TextView = itemView.findViewById(R.id.productType)
        val productName: TextView = itemView.findViewById(R.id.productName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resulted_product_card_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        val imageResId = ImageSetter.getImageResourceId(
            product.area,
            product.product_type,
            holder.itemView.context
        )
        holder.productImage.setImageResource(imageResId)
        holder.productType.text = product.product_type
        holder.productName.text = product.product_name

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}
