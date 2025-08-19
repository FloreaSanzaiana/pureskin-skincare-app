package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.SettingStep
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyStepsActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.modify_steps_page)
        val source = intent.getStringExtra("flag")

        val back_button: ImageButton=findViewById<ImageButton>(R.id.back)
        back_button.setOnClickListener {
            val intent = Intent(this, RoutineDetailsActivity::class.java)
            intent.putExtra("flag",source)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            startActivity(intent, options.toBundle())
        }

        var stepList = mutableListOf<SettingStep>()
        if(source!=null)
        {
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
            {
                for(s in savedRoutines.steps){
                    var step= true
                    var settingStep= SettingStep(step,s)
                    stepList.add(settingStep)
                }
            }
        }
        val dbManager = DatabaseManager(this)
        val routines=dbManager.getRoutineByType(source.toString())
        var routine_id=0
        var step_order=0
        if(routines!=null)
        {
            routine_id=routines.id
            step_order=routines.steps.size+1
        }

        var skincareRoutine = listOf(
            "Makeup remover",
            "Cleanser",
            "Toner",
            "Spray",
            "Serum",
            "Eye Care",
            "Oil",
            "Moisturiser",
            "Sunscreen"
        )
        if(source=="exfoliation")
        {
            skincareRoutine=listOf("Exfoliator","Peel")
        }

        for(a in skincareRoutine)
        {
            var ok=0
            for(s in stepList)
                if(a==s.step.step_name)
                {
                    ok=1
                }
            if(ok==0){
                var used= false
                var step=Step(0,routine_id,step_order,a,"",0)
                var settingStep= SettingStep(used,step)
                stepList.add(settingStep)
                step_order++
            }
        }



        val recyclerView = findViewById<RecyclerView>(R.id.recr)
        val adapter = ModifyStepAdapter(stepList,this,source.toString())


        val itemTouchHelperCallback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
            androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN,
            0
        ) {
            var fromPosition = -1
            var toPosition = -1

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (fromPosition == -1) {
                    fromPosition = viewHolder.adapterPosition
                }
                toPosition = target.adapterPosition

                val item = stepList.removeAt(viewHolder.adapterPosition)
                stepList.add(target.adapterPosition, item)
                adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                if (fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
                    val movedItem = stepList[toPosition]
                    Log.d("ItemDropped", "Item '${movedItem.step.id}' '${movedItem.step.step_name}' dropped at position: $toPosition")
                    val aux=Step(movedItem.step.id.toInt(),movedItem.step.routine_id,toPosition+1,movedItem.step.step_name,movedItem.step.description,movedItem.step.product_id)
                    modify_new_step(aux)
                    DatabaseManager(this@ModifyStepsActivity).moveStepToPosition(stepId = movedItem.step.id.toInt(), newPosition = toPosition+1)
                }

                fromPosition = -1
                toPosition = -1

                stepList.forEachIndexed { index, step ->
                    step.step.step_order = index + 1
                }
                adapter.notifyDataSetChanged()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = true
        }

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun modify_new_step(s:Step){
        val sharedPreferences =getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.modify_step("Bearer $token",s).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                    when (response.code()) {
                        200 -> {


                        }

                    }
                }
                override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }
}