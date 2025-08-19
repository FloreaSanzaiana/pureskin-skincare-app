package com.example.myapplication2.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Item
import com.example.myapplication2.data.model.RecommendationCard

class RecommendationAdapter(
    private val recommendations: List<RecommendationCard>,
    private val onItemClick: (RecommendationCard) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    inner class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.info_name)
        private val imageView: ImageView = itemView.findViewById(R.id.image_info)
        private val imageopen: ImageView = itemView.findViewById(R.id.image_open)
        private val openButton: ImageButton = itemView.findViewById(R.id.image_open)
        private val description: TextView=itemView.findViewById(R.id.description)
        fun bind(recommendation: RecommendationCard) {
            titleTextView.text = recommendation.title
            imageView.setImageResource(recommendation.imageResId)
            description.text=recommendation.description

            val background = GradientDrawable()
            background.setColor(
                if (titleTextView.text == "Recommend a product") {
                    Color.parseColor("#003366")
                } else {
                    Color.parseColor("#C2E3F3")
                }
            )
            if (titleTextView.text == "Recommend a product") {
              titleTextView.setTextColor(Color.parseColor("#FFFFFF"))
                description.setTextColor(Color.parseColor("#FFFFFF"))
                imageView.setImageResource(R.drawable.skincare)
                imageopen.setImageResource(R.drawable.open_white)
            } else {
                titleTextView.setTextColor(Color.parseColor("#003366"))
                description.setTextColor(Color.parseColor("#003366"))
            }
            background.cornerRadius = 50f
            background.setStroke(1, Color.parseColor("#E0E0E0"))

            itemView.background = background

            openButton.setOnClickListener {
                onItemClick(recommendation)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size
}
