package com.example.myapplication2.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.RoutineRecommended
import com.example.myapplication2.util.ImageSetter

class RoutineResultAdapter(private val routineRecommendedList: List<RoutineRecommended>, private val context: Context, private val onRoutineClick: (RoutineRecommended) -> Unit) :
    RecyclerView.Adapter<RoutineResultAdapter.RoutineResultViewHolder>() {

    class RoutineResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewStep: TextView = itemView.findViewById(R.id.textView19)
        val textViewProductName: TextView = itemView.findViewById(R.id.productName)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val textViewIndex: TextView = itemView.findViewById(R.id.textView20)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineResultViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.routine_resulted_card, parent, false)
        return RoutineResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoutineResultViewHolder, position: Int) {
        val routineRecommended = routineRecommendedList[position]

        holder.textViewStep.text = routineRecommended.step

        holder.textViewIndex.text = (position + 1).toString()

        holder.textViewProductName.text = routineRecommended.product.product_name

        val imageResId = ImageSetter.getImageResourceId(
            routineRecommended.product.area,
            routineRecommended.product.product_type,
            context
        )
        holder.productImage.setImageResource(imageResId)
        holder.itemView.setOnClickListener {
            onRoutineClick(routineRecommended)
        }
    }

    override fun getItemCount(): Int {
        return routineRecommendedList.size
    }
}
