package com.example.myapplication2.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Item

class ItemAdapter(
    private val context: Context,
    private val itemList: List<Item>,
    private val hideLastButton: Boolean,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.info_name)
        val descText: TextView = view.findViewById(R.id.content)
        val imageView: ImageView = view.findViewById(R.id.image_info)
        val imageButton: ImageButton = view.findViewById(R.id.image_open)

        fun bind(item: Item, isLastItem: Boolean) {
            titleText.text = item.title
            descText.text = item.description
            imageView.setImageResource(item.imageResource)

            if (hideLastButton && isLastItem) {
                titleText.setTextColor(Color.parseColor("#D32F2F"))
            } else {
                imageButton.visibility = View.VISIBLE
            }

            imageButton.setOnClickListener {
                onClick(item)
            }}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        val isLastItem = position == itemList.size - 1
        holder.bind(item, isLastItem)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
