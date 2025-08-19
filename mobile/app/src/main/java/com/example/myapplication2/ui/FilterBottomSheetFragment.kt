package com.example.myapplication2.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.myapplication2.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var filterListener: FilterListener

    interface FilterListener {
        fun onFiltersApplied()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterListener) {
            filterListener = context
        } else {
            throw ClassCastException("$context must implement FilterListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)

        val hasSpfCheckBox = view.findViewById<CheckBox>(R.id.hasSpf)
        val faceCheckBox = view.findViewById<CheckBox>(R.id.facearea)
        val lipsCheckBox = view.findViewById<CheckBox>(R.id.liparea)
        val eyesCheckBox = view.findViewById<CheckBox>(R.id.eyearea)
        val nightCheckBox = view.findViewById<CheckBox>(R.id.nighttime)
        val dayCheckBox = view.findViewById<CheckBox>(R.id.daytime)

        val productTypeIds = listOf(
            R.id.remover, R.id.cleanser, R.id.spray, R.id.oil,
            R.id.moisturiser, R.id.serum, R.id.mask, R.id.peel,
            R.id.toner, R.id.exfoliator, R.id.sunscreen
        )

        val showFilteredButton: Button = view.findViewById(R.id.showFilteredButton)
        showFilteredButton.setOnClickListener {
            val hasSpf = hasSpfCheckBox.isChecked
            prefs.edit().putBoolean("hasSpf", hasSpf).apply()

            val selectedAreas = mutableSetOf<String>()
            if (faceCheckBox.isChecked) selectedAreas.add("face")
            if (lipsCheckBox.isChecked) selectedAreas.add("lip")
            if (eyesCheckBox.isChecked) selectedAreas.add("eye")
            prefs.edit().putStringSet("area", selectedAreas).apply()

            val selectedTimes = mutableSetOf<String>()
            if (nightCheckBox.isChecked) selectedTimes.add("night")
            if (dayCheckBox.isChecked) selectedTimes.add("day")
            prefs.edit().putStringSet("timeOfDay", selectedTimes).apply()

            val selectedTypes = mutableSetOf<String>()
            productTypeIds.forEach { id ->
                val cb = view.findViewById<CheckBox>(id)
                if (cb.isChecked) selectedTypes.add(cb.text.toString())
            }
            prefs.edit().putStringSet("productTypes", selectedTypes).apply()

            filterListener.onFiltersApplied()

            dismiss()
        }
    }
}
