package com.example.myapplication2.util

import android.content.Context
import com.example.myapplication2.R

object ImageSetter2 {

    fun getImageResourceId2(area: String, tip: String, context: Context): Int {
        val tipLower = tip.lowercase().trim()
        val areaLower = area.lowercase().trim()

        return when (tipLower) {
            "serum" -> R.drawable.serum_cartoon
            "moisturiser" -> R.drawable.moisturiser_cartoon
            "cleanser" -> R.drawable.cleanser_cartoon
            "toner" -> R.drawable.toner_cartoon
            "exfoliator" ->R.drawable.exfoliator_cartoon
            "peel" ->R.drawable.peel_cartoon
            "eye care"->R.drawable.eye_care_cartoon
            "sunscreen"->R.drawable.spf_cartoon_2
            "spray"->R.drawable.spray_cartoon
            "oil" ->R.drawable.oil_cartoon
            "makeup remover" ->R.drawable.makeup_remover_cartoon
            "mask" -> {
                when (areaLower) {
                    "face" -> R.drawable.face_mask_cartoon
                    "eye" -> R.drawable.eye_mask_new
                    "lip" -> R.drawable.lip_mask_new
                    else -> R.drawable.face_mask_cartoon
                }
            }
            else -> R.drawable.moisturiser_cartoon
        }
    }

}
