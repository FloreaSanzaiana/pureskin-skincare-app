package com.example.myapplication2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.RoutineCompletionDetails

class CompletedRoutinesAdapter(
    private val routines: List<RoutineCompletionDetails>,
    private val onRoutineClick: (RoutineCompletionDetails) -> Unit
) : RecyclerView.Adapter<CompletedRoutinesAdapter.RoutineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_simple, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(routines[position])
    }

    override fun getItemCount(): Int = routines.size

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val routineIcon: ImageView = itemView.findViewById(R.id.routineIcon)
        private val routineName: TextView = itemView.findViewById(R.id.routineName)
        private val completionBadge: ImageView = itemView.findViewById(R.id.completionBadge)
        private val routineProgress: ProgressBar = itemView.findViewById(R.id.routineProgress)
        private val stepsCount: TextView = itemView.findViewById(R.id.stepsCount)
        private val productsText: TextView = itemView.findViewById(R.id.productsText)

        fun bind(routine: RoutineCompletionDetails) {
            val formattedRoutineName = routine.routine_type
                ?.split(" ")
                ?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                ?: "Unknown"

            routineName.text = "$formattedRoutineName Routine"
            stepsCount.text = "${routine.steps.size}/${routine.max_steps} steps completed"

            val progressPercentage = ((routine.steps?.size ?: 0) * 100) / (routine.max_steps.takeIf { it > 0 } ?: 1)
            routineProgress.progress = progressPercentage

            val (iconRes, iconColor) = getRoutineIconAndColor(routine.routine_type.toString())
            routineIcon.setImageResource(iconRes)
            routineIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(
                itemView.context.getColor(iconColor)
            )

            completionBadge.visibility = if (progressPercentage == 100) View.VISIBLE else View.GONE

            val productsString = routine.steps.joinToString(", ")
            productsText.text = productsString


        }

        private fun getRoutineIconAndColor(routineName: String): Pair<Int, Int> {
            return when {
                routineName.contains("Morning", ignoreCase = true) ->
                    Pair(R.drawable.sun, R.color.light_pastel_blue)
                routineName.contains("Evening", ignoreCase = true) ->
                    Pair(R.drawable.moon, R.color.light_pastel_blue)
                routineName.contains("Exfoliation", ignoreCase = true) ->
                    Pair(R.drawable.exfoliation, R.color.light_pastel_blue)
                routineName.contains("Face Mask", ignoreCase = true) ->
                    Pair(R.drawable.face_mask_icon, R.color.light_pastel_blue)
                routineName.contains("Eye Mask", ignoreCase = true) ->
                    Pair(R.drawable.eye_mask_icon, R.color.light_pastel_blue)
                routineName.contains("Lip Mask", ignoreCase = true) ->
                    Pair(R.drawable.lip_mask, R.color.light_pastel_blue)
                routineName.contains("Spf", ignoreCase = true) ->
                    Pair(R.drawable.sunscreen_icon, R.color.light_pastel_blue)
                else ->
                    Pair(R.drawable.ic_skincare, R.color.light_pastel_blue)
            }
        }
    }
}