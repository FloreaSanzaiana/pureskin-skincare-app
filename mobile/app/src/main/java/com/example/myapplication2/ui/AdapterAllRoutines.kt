package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.ui.EditRoutinesActivity

class AdapterAllRoutines(
    private val routines: MutableList<UserRoutines>,
    private val context:Context,
    private val onItemClick: (UserRoutines) -> Unit
) : RecyclerView.Adapter<AdapterAllRoutines.RoutineViewHolder>() {

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.text)
        private val time: TextView = itemView.findViewById(R.id.textt)
        private val icon: ImageView = itemView.findViewById(R.id.image)
        private val button: ImageView = itemView.findViewById(R.id.image_open)
        private val checkbox: CheckBox=itemView.findViewById(R.id.checkbox)

        fun bind(item: UserRoutines) {
            when (item.routine_type) {
                "morning" -> {
                    icon.setImageResource(R.drawable.sun)
                    name.text = "Morning Routine"
                    time.text = "Daily"
                }

                "evening" -> {
                    icon.setImageResource(R.drawable.moon)
                    name.text = "Evening Routine"
                    time.text = "Daily"
                }

                "face mask" -> {
                    icon.setImageResource(R.drawable.face_mask_icon)
                    name.text = "Face Mask"
                    time.text = "Weekly"
                }

                "exfoliation" -> {
                    icon.setImageResource(R.drawable.exfoliation)
                    name.text = "Face Exfoliation"
                    time.text = "Weekly"
                }

                "eye mask" -> {
                    icon.setImageResource(R.drawable.eye_mask_icon)
                    name.text = "Eye Mask"
                    time.text = "Weekly"
                }

                "lip mask" -> {
                    icon.setImageResource(R.drawable.lip_mask)
                    name.text = "Lip Mask"
                    time.text = "Weekly"
                }

                "spf" -> {
                    icon.setImageResource(R.drawable.sunscreen_icon)
                    name.text = "SPF"
                    time.text = "Daily"

                }
                "log" -> {
                    icon.setImageResource(R.drawable.diary)
                    name.text = "Skin Diary Log"
                    time.text = "Daily"

                }

                else -> {
                    icon.setImageResource(R.drawable.user) // fallback icon dacă ai una
                    name.text = item.routine_type.replaceFirstChar { it.uppercaseChar() }
                    time.text = ""
                }
            }
            checkbox.isEnabled = true
            checkbox.isClickable = true
            if(item.routine_type=="log"){

                val db= DatabaseManager(context)
                val r=db.getTodayDailyLogContent()
                if (r!=null) checkbox.isChecked=true else checkbox.isChecked=false
                checkbox.isEnabled = false
                checkbox.isClickable = false
            }
            else if(item.routine_type!="spf")
            {
                val db= DatabaseManager(context)
                val r=db.getTodayCompletedRoutineIds()
                Log.d("debug",r.toString())
                var ok=0
                for(i in r) if(item.id==i) ok=1

                if(ok==1) checkbox.isChecked=true else checkbox.isChecked=false

                checkbox.isEnabled = false
                checkbox.isClickable = false
            }
            else{
                val db= DatabaseManager(context)
                val r=db.getTodayCompletedSpfId()
                Log.d("debug",r.toString())

                if(r!=null) checkbox.isChecked=true else checkbox.isChecked=false

                checkbox.isEnabled = false
                checkbox.isClickable = false
            }


            button.setOnClickListener {
                if(item.routine_type=="log"){
                    val intent = Intent(context, DailyLogActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    context.startActivity(intent, options.toBundle())
                    Log.d("log"," se doreste a intra in log daily")
                }else
                onItemClick(item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_routine_item_card, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(routines[position])
    }

    override fun getItemCount(): Int = routines.size

    fun setItems(newRoutines: List<UserRoutines>) {
        routines.clear()
        routines.addAll(newRoutines)
        notifyDataSetChanged()
    }

    fun addItem(item: UserRoutines) {
        routines.add(item)
        notifyItemInserted(routines.size - 1)
    }
}
