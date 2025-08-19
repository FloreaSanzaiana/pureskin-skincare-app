package com.example.myapplication2.util

import android.content.Context
import com.example.myapplication2.R

object ImageSetter {

    fun getImageResourceId(area: String, tip: String, context: Context): Int {
        val tipLower = tip.lowercase().trim()
        val areaLower = area.lowercase().trim()

        return when (tipLower) {
            "serum" -> R.drawable.serum
            "moisturiser" -> R.drawable.moisturiser
            "cleanser" -> R.drawable.cleanser
            "toner" -> R.drawable.toner
            "exfoliator" ->R.drawable.exfoliator
            "peel" ->R.drawable.face_peeling
            "eye care"->R.drawable.eye_cream
            "sunscreen"->R.drawable.sunscreen
            "spray"->R.drawable.spray
            "oil" ->R.drawable.oil
            "makeup remover" ->R.drawable.makeup_remover
            "mask" -> {
                when (areaLower) {
                    "face" -> R.drawable.face_mask
                    "eye" -> R.drawable.eye_mask
                    "lip" -> R.drawable.lips_mask
                    else -> R.drawable.face_mask
                }
            }
            else -> R.drawable.moisturiser
        }
    }

}
