package com.example.myapplication2.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.util.ImageSetter
import com.example.myapplication2.util.ImageSetter2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoutineDetailsAdapter(private val productList: List<Step>, private val listener: OnStepCompletionListener,private var flag: Boolean ,private val area: String,private var routine_name: String) :
    RecyclerView.Adapter<RoutineDetailsAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val type: TextView = itemView.findViewById(R.id.productType)
        val button: Button = itemView.findViewById(R.id.textView8)
        val radio: MaterialCheckBox = itemView.findViewById(R.id.radioButton2)
        val number: TextView = itemView.findViewById(R.id.textView7)
        val delete_button: ImageButton=itemView.findViewById<ImageButton>(R.id.remove_product)
        val vew: View=itemView.findViewById<View>(R.id.view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.routine_step_item_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val step = productList[position]
        val cardView = holder.itemView as MaterialCardView
        val db= DatabaseManager(holder.itemView.context)
         val my_step=step.product_id
       if( my_step!=null){
           if(my_step>0){
               holder.vew.visibility= View.VISIBLE
               cardView.setCardBackgroundColor(Color.WHITE)
               cardView.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.pastel_blue)
               cardView.strokeWidth = 6
               cardView.elevation = 8f
               holder.type.visibility= View.GONE
               holder.delete_button.visibility= View.VISIBLE
               holder.button.visibility= View.GONE
               holder.number.text = step.step_order.toString()
               val aux=step.product_id
               if(aux!=null)
               {
                   val prod=db.getProductById(aux.toInt())
                   Log.d("product_id",aux.toString())
                   Log.d("product_id",prod.toString())
                   if(prod!=null)
                       holder.name.text=prod.name
                   val context = holder.itemView.context

                   if(step.step_name=="spf"){
                       val imageResId = ImageSetter.getImageResourceId("", "sunscreen", context)
                       holder.image.setImageResource(imageResId)
                   }else{
                       val imageResId = ImageSetter.getImageResourceId(area, step.step_name.lowercase(), context)
                       holder.image.setImageResource(imageResId)
                   }

               }
           }
           else{

               if(step.step_name=="spf"){
                   holder.vew.visibility= View.GONE
                   cardView.setCardBackgroundColor(Color.parseColor("#E8E8EA"))
                   cardView.strokeColor = Color.parseColor("#D1D1D6")
                   cardView.strokeWidth = 4
                   cardView.elevation = 8f
                   holder.type.visibility= View.VISIBLE
                   holder.delete_button.visibility= View.GONE
                   holder.button.visibility= View.VISIBLE
                   holder.name.text = "Sunscreen"
                   holder.type.text="Protects skin from harmful UV rays, preventing premature aging and sun damage"
                   holder.number.text = step.step_order.toString()
                   val context = holder.itemView.context

                   val imageResId = ImageSetter2.getImageResourceId2("", "sunscreen", context)
                   holder.image.setImageResource(imageResId)
               }

               else{
               holder.vew.visibility= View.GONE
               cardView.setCardBackgroundColor(Color.parseColor("#E8E8EA"))
               cardView.strokeColor = Color.parseColor("#D1D1D6")
               cardView.strokeWidth = 4
               cardView.elevation = 8f
               holder.type.visibility= View.VISIBLE
               holder.delete_button.visibility= View.GONE
               holder.button.visibility= View.VISIBLE
               holder.name.text = step.step_name
               if(holder.name.text!="Sunscreen")
               {
                   holder.type.text = step.description
               }
               else
                   holder.type.text="Protects skin from harmful UV rays, preventing premature aging and sun damage"
               holder.number.text = step.step_order.toString()
               val context = holder.itemView.context

               val imageResId = ImageSetter2.getImageResourceId2(area, step.step_name.lowercase(), context)
               holder.image.setImageResource(imageResId)
           }}

       }

        holder.radio.setOnCheckedChangeListener(null)

        val steps_list=db.getTodayCompletedStepIds()
        val spf_list=db.getTodayCompletedSpfId()
        Log.d("steps",steps_list.toString())
        var ok=0
        var ok2=0
        for( s in steps_list) if(s==step.id) ok=1
        if(spf_list!=null && step.step_name=="spf") ok2=1

        val test=db.getAllRoutineCompletions()
        Log.d("DEBUG","test")
        holder.radio.isChecked=false
        if(ok==1 || ok2==1) holder.radio.isChecked=true else holder.radio.isChecked=false
        holder.radio.isEnabled = true
        holder.radio.isClickable = true
        if (flag) {
            holder.radio.isEnabled = false
            holder.radio.isClickable = false
        } else {
            holder.radio.isEnabled = true
            holder.radio.isClickable = true
            holder.radio.setOnCheckedChangeListener { _, isChecked ->
                listener.onStepCompleted(step.step_name, isChecked)
            }
        }

        holder.delete_button.setOnClickListener {
           if(step.step_name!="spf"){
               val stepp=Step(step.id,step.routine_id,step.step_order,step.step_name,step.description,-1)
               deleteProduct(stepp,holder.itemView.context)
               holder.vew.visibility= View.GONE
               cardView.setCardBackgroundColor(Color.parseColor("#E8E8EA"))
               cardView.strokeColor = Color.parseColor("#D1D1D6")
               cardView.strokeWidth = 4
               cardView.elevation = 8f
               holder.type.visibility= View.VISIBLE
               holder.delete_button.visibility= View.GONE
               holder.button.visibility= View.VISIBLE
               holder.name.text = step.step_name
               if(holder.name.text!="Sunscreen")
               {
                   holder.type.text = step.description
               }
               else
                   holder.type.text="Protects skin from harmful UV rays, preventing premature aging and sun damage"
               holder.number.text = step.step_order.toString()
               val context = holder.itemView.context

               val imageResId = ImageSetter2.getImageResourceId2(area, step.step_name.lowercase(), context)
               holder.image.setImageResource(imageResId)
           }else{
               val db= DatabaseManager(holder.itemView.context)
               val spf=db.getSpf()
               Log.d("spf","buton apasat")
               if(spf!=null)
               {
                   Log.d("spf","buton apasat2")
                   spf.product_id=-1
                   deleteSpfProduct(spf,holder.itemView.context)
                   holder.vew.visibility= View.GONE
                   cardView.setCardBackgroundColor(Color.parseColor("#E8E8EA"))
                   cardView.strokeColor = Color.parseColor("#D1D1D6")
                   cardView.strokeWidth = 4
                   cardView.elevation = 8f
                   holder.type.visibility= View.VISIBLE
                   holder.delete_button.visibility= View.GONE
                   holder.button.visibility= View.VISIBLE
                   holder.name.text = "Sunscreen"
                   if(holder.name.text!="Sunscreen")
                   {
                       holder.type.text = step.description
                   }
                   else
                       holder.type.text="Protects skin from harmful UV rays, preventing premature aging and sun damage"
                   holder.number.text = step.step_order.toString()
                   val context = holder.itemView.context

                   val imageResId = ImageSetter2.getImageResourceId2("", "sunscreen", context)
                   holder.image.setImageResource(imageResId)
               }
           }
        }
        holder.button.setOnClickListener {


            val intent = Intent(holder.itemView.context, ProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION


            if(step.step_name.lowercase()=="sunscreen" || step.step_name.lowercase()=="spf")
             intent.putExtra("step_name", "sunscreen")
            else
             intent.putExtra("step_name", step.step_name)

            intent.putExtra("step_area", area)
            intent.putExtra("routine_name", routine_name)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = productList.size

    private fun deleteProduct(step: Step, context: Context) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            return
        }

        val call = apiService.add_products_to_step("Bearer $token", step)
        call.enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
            override fun onResponse(
                call: Call<com.example.myapplication2.data.model.Response>,
                response: Response<com.example.myapplication2.data.model.Response>
            ) {
                if (response.isSuccessful) {
                    val success = DatabaseManager(context).removeProductFromStep(step.id)
                    if (success == true) {
                        Toast.makeText(context, "Product removed successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Eroare la ștergerea produsului", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                Toast.makeText(context, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                Log.e("DeleteProduct", "Network error: ${t.message}")
            }
        })
    }

    private fun deleteSpfProduct(step: SpfRoutine, context: Context) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            return
        }

        val call = apiService.add_products_to_spf("Bearer $token", step)
        call.enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
            override fun onResponse(
                call: Call<com.example.myapplication2.data.model.Response>,
                response: Response<com.example.myapplication2.data.model.Response>
            ) {
                if (response.isSuccessful) {
                    val success = DatabaseManager(context).removeProductFromSpfRoutine(step.id)
                    if (success == true) {
                        Toast.makeText(context, "Product removed successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Eroare la ștergerea produsului", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                Toast.makeText(context, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                Log.e("DeleteProduct", "Network error: ${t.message}")
            }
        })
    }
}
