package com.example.myapplication2.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.SettingStep
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.util.ImageSetter2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyStepAdapter(private val productList:  MutableList<SettingStep>,private val context: Context, private val routine_type: String) :
    RecyclerView.Adapter<ModifyStepAdapter.ProductViewHolder>() {
    var new_step=Step(0,0,0,"","",0)

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val number: TextView = itemView.findViewById(R.id.textView7)
        val name: TextView = itemView.findViewById(R.id.productName)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val option: ImageView = itemView.findViewById(R.id.option)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_step_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val step = productList[position]
        holder.name.text = step.step.step_name

        val context = holder.itemView.context

        val imageResId = ImageSetter2.getImageResourceId2("", step.step.step_name.lowercase(), context)
        holder.image.setImageResource(imageResId)

        if(step.used==true){
            holder.option.setImageResource(R.drawable.delete)
            holder.number.text = step.step.step_order.toString()
        }
        else{
            holder.option.setImageResource(R.drawable.add)
            holder.number.text = ""
        }
        holder.option.setOnClickListener {
            if (step.used == true) {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val removedStep = productList.removeAt(currentPosition)
                    removedStep.used = false
                    removedStep.step.step_order = -1
                    productList.add(removedStep)

                    notifyItemMoved(currentPosition, productList.size - 1)
                    notifyItemChanged(productList.size - 1)

                    var order = 1
                    for (item in productList) {
                        if (item.used == true) {
                            item.step.step_order = order
                            order++
                        }
                    }
                    notifyDataSetChanged()
                }

                holder.option.setImageResource(R.drawable.add)
                holder.number.text = ""

                val dbManager = DatabaseManager(context)
                val id = step.step.id
                deleteStep(step.step)
                dbManager.deleteStepById(id)
            }
            else {
                 val aux=step.step.step_name
                if (productList.any { it.step.step_name == aux && it.used==true}) {
                    Toast.makeText(context, "This product exists", Toast.LENGTH_SHORT).show()
                } else {


                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    step.used = true

                    val newOrder = productList.count { it.used } + 1
                    step.step.step_order = newOrder

                    productList.removeAt(currentPosition)

                    var insertPosition = productList.indexOfLast { it.used }
                    if (insertPosition == -1) insertPosition = 0 else insertPosition += 1
                    productList.add(insertPosition, step)

                    var order = 1
                    for (item in productList) {
                        if (item.used == true) {
                            item.step.step_order = order
                            order++
                        } else {
                            item.step.step_order = -1
                        }
                    }

                    notifyDataSetChanged()

                    holder.option.setImageResource(R.drawable.delete)
                    holder.number.text = step.step.step_order.toString()


                    addStep(step.step)
                }
            }

        }}




    }

    override fun getItemCount(): Int = productList.size

    fun deleteStep(s:Step){
            val sharedPreferences =context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
            val token = sharedPreferences.getString("session_token", null)


            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            } else {
                val api = RetrofitInstance.instance
                api.delete_step("Bearer $token",s).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                    override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                        when (response.code()) {
                            200 -> {
                                if (response.isSuccessful) {
                                    val routines = response.body() ?: ""
                                    Log.e("Delete step", routines.toString())
                                }

                            }

                        }
                    }
                    override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {

                        Log.e("Error", t.message ?: "Unknown error")
                    }

                })}

    }

    fun addStep(s:Step){
        val sharedPreferences =context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.add_step("Bearer $token",s).enqueue(object : Callback<Step> {
                override fun onResponse(call: Call<Step>, response: Response<Step>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val auxx=response.body()
                                Log.d("StepAdded", "the response is null")

                                if(auxx!=null)
                                {
                                    Log.d("StepAdded", "Step added to DB: ${new_step.step_name}, id=${new_step.id}")

                                    new_step=auxx
                                    val dbManager = DatabaseManager(context)
                                    dbManager.insertStepAtEnd(new_step)


                                    val existingSteps = dbManager.getRoutineByType(routine_type)
                                    if(existingSteps!=null)
                                        {
                                            existingSteps.steps.last<Step>()
                                            Log.d("StepsBeforeInsert", "Steps before insert: ${existingSteps.steps.map { it.step_name }}")
                                        }

                                    dbManager.insertStepAtEnd(new_step)

                                }
                            }

                        }

                    }
                }
                override fun onFailure(call: Call<Step>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }

}
