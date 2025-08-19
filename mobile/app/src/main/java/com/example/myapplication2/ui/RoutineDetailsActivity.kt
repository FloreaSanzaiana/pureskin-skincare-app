package com.example.myapplication2.ui

import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.RoutineCompletion
import com.example.myapplication2.data.model.RoutineCompletionDetails
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserMessage
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class RoutineDetailsActivity : AppCompatActivity(), OnStepCompletionListener {
    private val completedSteps = mutableSetOf<String>()
    private lateinit var saveButton: MaterialButton
    private  lateinit var source: String
    private  lateinit var area_for_products_filtering: String
    private var flag_for_adapter=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routine_details_page)
        source = intent.getStringExtra("flag").toString()
        saveButton = findViewById<MaterialButton>(R.id.saveRoutineButton)
        val back: ImageButton=findViewById<ImageButton>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            startActivity(intent, options.toBundle())
        }

        area_for_products_filtering="face"


        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH)
        val currentDate = dateFormat.format(Date())
        val text: TextView=findViewById<TextView>(R.id.date)
        text.text=currentDate

        val edit_button: ImageButton=findViewById<ImageButton>(R.id.edit)
        var steps: MutableList<Step> =mutableListOf<Step>()

        val db= DatabaseManager(this)
        val r=db.getTodayCompletedRoutineIds()
        val this_routine=db.getRoutineByType(source.toString())
        val spf_list=db.getTodayCompletedSpfId()
        if(spf_list!=null && source=="spf") {
            saveButton.visibility= View.GONE
            flag_for_adapter=true
        } else saveButton.visibility= View.VISIBLE

        if(this_routine!=null){
            var ok=0
            for(i in r) if(this_routine.id==i) {ok=1; flag_for_adapter=true}

            if(ok==1) saveButton.visibility= View.GONE else saveButton.visibility= View.VISIBLE
        }



        if(source=="morning")
        {
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Morning Routine"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.morning)
            edit_button.visibility= View.VISIBLE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
              steps= savedRoutines.steps.toMutableList()

            edit_button.setOnClickListener {
                val intent = Intent(this, ModifyStepsActivity::class.java)
                intent.putExtra("flag","morning")
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }

        }
        else if(source=="evening"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Evening Routine"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.evening)
            edit_button.visibility= View.VISIBLE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
                steps= savedRoutines.steps.toMutableList()

            edit_button.setOnClickListener {
                val intent = Intent(this, ModifyStepsActivity::class.java)
                intent.putExtra("flag","evening")
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }
        }
        else if(source=="exfoliation"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Face Exfoliation"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.exfoliation_background)
           // edit_button.visibility= View.GONE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
                steps= savedRoutines.steps.toMutableList()
            edit_button.setOnClickListener {
                val intent = Intent(this, ModifyStepsActivity::class.java)
                intent.putExtra("flag","exfoliation")
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }
        }
        else if(source=="face mask"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Face Mask"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.facemask_backgorund)
            edit_button.visibility= View.GONE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
                steps= savedRoutines.steps.toMutableList()
        }
        else if(source=="eye mask"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Eye Mask"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.eyemask_background)
            edit_button.visibility= View.GONE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
                steps= savedRoutines.steps.toMutableList()
            area_for_products_filtering="eye"
        }
        else if(source=="lip mask"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Lip Mask"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.lipmask_background)
            edit_button.visibility= View.GONE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getRoutineByType(source)
            if(savedRoutines!=null)
                steps= savedRoutines.steps.toMutableList()
            area_for_products_filtering="lip"
        }
        else if(source=="spf"){
            val text: TextView=findViewById<TextView>(R.id.routine_type)
            text.text="Sunscreen Reminder"
            val back_image = findViewById<ImageView>(R.id.background)
            back_image.setImageResource(R.drawable.spf_sunscreen)
            edit_button.visibility= View.GONE
            val dbManager = DatabaseManager(this)
            val savedRoutines = dbManager.getSpf()
            if(savedRoutines!=null)
            {
                val step=Step(savedRoutines.id,0,1,"spf","",savedRoutines.product_id)
                steps.add(step)
            }
        }

        Log.d("product_id",steps.toString())
        val recyclerView: RecyclerView = findViewById(R.id.routiner)

        val textnull: TextView=findViewById<TextView>(R.id.textnull)

        if(steps.size<1){
            textnull.visibility=View.VISIBLE
        }else{
            textnull.visibility=View.GONE

        }
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val recycler = findViewById<RecyclerView>(R.id.routiner)
        recycler.minimumHeight = screenHeight - 500

        val adapter = RoutineDetailsAdapter(steps,this,flag_for_adapter,area_for_products_filtering,source)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val button: ImageButton=findViewById<ImageButton>(R.id.notification)
        button.setOnClickListener {
            val db= DatabaseManager(this)
            val aux=db.getRoutineByType(source.toString())
            val spf=db.getSpf()
            if(aux!=null )
            {
                val routine=Routine(aux.user_id,aux.routine_type,aux.notification_time,aux.notification_days,aux.id)
                if(spf!=null)
                Log.d("spf",spf.end_time.toString())
                else
                    Log.d("spf","spf e null")

                val bottomSheet = NotificationBottomPage(routine,null)
                bottomSheet.show(supportFragmentManager, "NotificationBottomSheet")
            }
            else if(spf!=null){
                val routine=Routine(0,"","","",0)
                val bottomSheet = NotificationBottomPage(routine,spf)
                bottomSheet.show(supportFragmentManager, "NotificationBottomSheet")
            }

        }


        val scaleUp = ObjectAnimator.ofFloat(saveButton, "scaleX", 1.0f, 1.05f)
        val scaleDown = ObjectAnimator.ofFloat(saveButton, "scaleY", 1.0f, 1.05f)

        scaleUp.duration = 1000
        scaleDown.duration = 1000
        scaleUp.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleUp.repeatMode = ObjectAnimator.REVERSE
        scaleDown.repeatMode = ObjectAnimator.REVERSE

        scaleUp.start()
        scaleDown.start()


        saveButton.setOnClickListener {

            if(completedSteps.size>0)
            {
                val db= DatabaseManager(this)
                val sharedPreferences =
                    getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val user_id=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                var routine_complete= RoutineCompletion(0,0,"",0,mutableListOf<Int>(),0)
                if(source!="spf")
                {
                    val routine=db.getRoutineByType(source.toString())
                    var routine_id=0
                    if(routine!=null)
                    {
                        routine_id=routine.id
                        Log.d("save","routine id: "+routine_id.toString())
                    }
                    val real_steps = mutableListOf<Int>()
                    for( m in completedSteps){

                        var step_id=0
                        var step=db.getStepByRoutineAndType(routine_id,m)
                        if(step!=null) step_id=step.id

                        real_steps.add(step_id)
                        Log.d("save","step id: "+step_id.toString()+"  step name: "+m)

                    }
                    routine_complete= RoutineCompletion(0,user_id,"",routine_id,real_steps,null)
                }
                else{
                    val spf=db.getSpf()
                    var spf_id=0
                    if(spf!=null)
                    {
                        spf_id=spf.id
                        Log.d("save","spf id: "+spf.id.toString())
                    }
                    routine_complete= RoutineCompletion(0,user_id,"",null,mutableListOf<Int>(),spf_id)

                }
                saveRoutine(routine_complete)
            }
            else{
                Toast.makeText(this@RoutineDetailsActivity, "No steps selected. Please check at least one step before saving.", Toast.LENGTH_SHORT).show()

            }
        }

    }
    override fun onStepCompleted(stepName: String, isCompleted: Boolean) {
        if (isCompleted) {
            Log.d("checked",stepName)
            completedSteps.add(stepName)
        } else {
            Log.d("unchecked",stepName)
            completedSteps.remove(stepName)
        }

    }

    private fun saveRoutine(routine: RoutineCompletion){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val user_id=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1

        val call = apiService.send_routine_completion("Bearer $token",routine)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@RoutineDetailsActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<RoutineCompletion> {
                override fun onResponse(call: Call<RoutineCompletion>, response: Response<RoutineCompletion>) {
                    if (response.isSuccessful) {
                        val new_routine = response.body()
                        if(new_routine!=null){
                            val db= DatabaseManager(this@RoutineDetailsActivity)
                            db.insertRoutineCompletion(new_routine)
                            Log.d("new_routine",new_routine.toString())
                            saveButton.visibility= View.GONE
                            Toast.makeText(this@RoutineDetailsActivity, "Routine completion saved successfully.", Toast.LENGTH_SHORT).show()
                            var details_steps=db.getStepNamesByIds(new_routine.steps)
                            var routine_aux=db.getRoutineByType(source)
                            var max_step_size=0
                            if(routine_aux!=null) max_step_size=routine_aux.steps.size
                            var details_routine= RoutineCompletionDetails(0,"",user_id,source,details_steps,max_step_size)
                            if(source=="spf") details_routine= RoutineCompletionDetails(0,"",user_id,"SPF",mutableListOf<String>("Sunscreen"),1)
                            if(source=="exfoliation") details_routine= RoutineCompletionDetails(0,"",user_id,"Face Exfoliation",details_steps,max_step_size)
                            saveDetailsRoutine(details_routine)
                        }

                    } else {

                        Toast.makeText(this@RoutineDetailsActivity, "Error saving routine completion", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RoutineCompletion>, t: Throwable) {
                    Toast.makeText(this@RoutineDetailsActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveDetailsRoutine(routine: RoutineCompletionDetails){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)


        val call = apiService.insertRoutineCompletionDetails("Bearer $token",routine)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@RoutineDetailsActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<RoutineCompletionDetails> {
                override fun onResponse(call: Call<RoutineCompletionDetails>, response: Response<RoutineCompletionDetails>) {
                    if (response.isSuccessful) {
                        val new_routine = response.body()
                        if(new_routine!=null){
                            val db= DatabaseManager(this@RoutineDetailsActivity)
                             db.insertRoutineCompletionDetails(new_routine)
                            Log.d("details",db.getAllRoutineCompletionDetails().toString())

                        }

                    } else {

                        Toast.makeText(this@RoutineDetailsActivity, "Error saving routine completion details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RoutineCompletionDetails>, t: Throwable) {
                    Toast.makeText(this@RoutineDetailsActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    }
