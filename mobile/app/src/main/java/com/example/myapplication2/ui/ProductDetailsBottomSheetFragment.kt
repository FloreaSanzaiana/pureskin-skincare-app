package com.example.myapplication2.ui

import android.R.attr.repeatCount
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.util.ImageSetter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailsBottomSheetFragment(private val product: Product) : BottomSheetDialogFragment() {
    private var routine_name: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        routine_name= arguments?.getString("routine_name")
        return inflater.inflate(R.layout.fragment_product_details_bottom_sheet, container, false)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                val displayMetrics = resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.75).toInt()

                it.layoutParams.height = desiredHeight
                behavior.peekHeight = desiredHeight
                behavior.maxHeight = desiredHeight
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }

        return dialog
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val img=R.id.productImage

        val imageView: ImageView = view?.findViewById(img) ?: return
        val imageResId = ImageSetter.getImageResourceId(product.area, product.type, requireContext())
        imageView.setImageResource(imageResId)

        imageView.post {
            imageView.layoutParams.height = imageView.height
        }

        val productNameTextView = view.findViewById<TextView>(R.id.productName)
        val productTypeTextView = view.findViewById<TextView>(R.id.productType)
        val productAreaTextView = view.findViewById<TextView>(R.id.productArea)
        val productTimeTextView = view.findViewById<TextView>(R.id.productTime)
        val productSpfTextView = view.findViewById<TextView>(R.id.productSpf)
        val productUrlTextView = view.findViewById<TextView>(R.id.url)
        val productIngredientsTextView = view.findViewById<TextView>(R.id.ingredients)
        val productIngredientsIrritating = view.findViewById<TextView>(R.id.ingredients_irritating)

        productNameTextView.text = product.name
        productTypeTextView.text = "\nProduct Type: ${product.type}\n\n"
        productAreaTextView.text = "Best for your skin: ${product.area}\n\n"
        productTimeTextView.text = "Perfect for: ${product.time}\n\n"

        productSpfTextView.text = when {
            product.spf == -1 -> "Sun Protection: -\n\n"
            product.spf == 0 -> "Sun Protection: Present but unspecified\n\n"
            else -> "Sun Protection: SPF ${product.spf}\n\n"
        }

        productUrlTextView.apply {
            text = "Shop Now: ${product.url}\n\n"
            if (product.url.isNotEmpty() && Patterns.WEB_URL.matcher(product.url).matches()) {
                Linkify.addLinks(this, Linkify.WEB_URLS)
                setLinkTextColor(ContextCompat.getColor(context, R.color.blue_pastel_dark))
                movementMethod = LinkMovementMethod.getInstance()
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                text = "Invalid URL"
            }
        }

        val formattedIngredients = product.ingredients
            .removeSurrounding("[", "]")
            .replace("\'", "")
            .replace(",", ", ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        productIngredientsTextView.text = "Key Ingredients: \n\n$formattedIngredients"

        val formattedIngredientsIrritations = product.irritating_ingredients
            .removeSurrounding("[", "]")
            .replace("'", "")
            .replace(",", ", ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        val displayText = if (formattedIngredientsIrritations.isEmpty()) {
            "-"
        } else {
            formattedIngredientsIrritations
        }

        productIngredientsIrritating.text = "\n\nIngredients that may cause irritation:\n\n$displayText"
        val morning_button:MaterialButton=view.findViewById<MaterialButton>(R.id.morningButton)
        val evening_button:MaterialButton=view.findViewById<MaterialButton>(R.id.eveningButton)
        val routine_button: MaterialButton=view.findViewById<MaterialButton>(R.id.routineButton)
        if(product.type.lowercase()=="mask"){
            routine_button.visibility= View.VISIBLE
            morning_button.visibility= View.GONE
            evening_button.visibility= View.GONE
            if(product.area.lowercase()=="face")
                routine_button.text="FACE MASK ROUTINE"
            else if(product.area.lowercase()=="eye")
                routine_button.text="EYE MASK ROUTINE"
            else if(product.area.lowercase()=="lip")
                routine_button.text="LIP MASK ROUTINE"
        }
        else if(product.type.lowercase()=="exfoliator" || product.type.lowercase()=="peel"){
            morning_button.visibility= View.GONE
            evening_button.visibility= View.GONE
            routine_button.visibility= View.VISIBLE
            routine_button.text="EXFOLIATION  ROUTINE"
        }
        else if(product.type.lowercase()=="sunscreen"){
            morning_button.visibility= View.VISIBLE
            evening_button.visibility= View.VISIBLE
            routine_button.visibility= View.VISIBLE
            routine_button.text="SUNSCREEN  ROUTINE"
        }
        else{
            routine_button.visibility= View.GONE
            morning_button.visibility= View.VISIBLE
            evening_button.visibility= View.VISIBLE
        }

        if(routine_name=="face mask" || routine_name=="eye mask" || routine_name=="lip mask" || routine_name=="exfoliator"|| routine_name=="exfoliation" || routine_name=="sunscreen" || routine_name=="spf" || routine_name=="mask")
        {
            startPulseAnimation(routine_button)
        }
        else if(routine_name=="morning")
        {
            startPulseAnimation(morning_button)
        }
        else if(routine_name=="evening"){
            startPulseAnimation(evening_button)
        }




        morning_button.setOnClickListener {

            val db= DatabaseManager(view.context)
            val routine=db.getRoutineByType("morning")
            if(routine!=null)
            {
                var product_type = product.type.lowercase().replaceFirstChar { it.uppercase() }
                if(product.type.lowercase()=="eye care") product_type="Eye Care"
                if(product.type.lowercase()=="makeup remover") product_type="Makeup remover"

                val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                Log.d("step",routine.id.toString()+" "+product_type.trim())
                if(stepp!=null){
                    val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                    addProduct(new_step,view)
                }
                else{
                    val aux_step=Step(0,routine.id,0,product_type,"",0)
                    addStep(aux_step,view.context,"morning",view, routine.id,product_type)

                }
            }
            else{
                Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
            }
        }
        evening_button.setOnClickListener {

            val db= DatabaseManager(view.context)
            val routine=db.getRoutineByType("evening")
            if(routine!=null)
            {
                var product_type = product.type.lowercase().replaceFirstChar { it.uppercase() }
                if(product.type.lowercase()=="eye care") product_type="Eye Care"
                if(product.type.lowercase()=="makeup remover") product_type="Makeup remover"

                val aux=db.getRoutineByType("evening")
                if(aux!=null){ Log.d("step",aux.steps.toString())}
                val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                Log.d("step",routine.id.toString()+" "+product_type.trim())
                if(stepp!=null){
                    val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                    addProduct(new_step,view)
                }
                else{
                    val aux_step=Step(0,routine.id,0,product_type,"",0)
                    addStep(aux_step,view.context,"evening",view, routine.id,product_type)

                }
            }
            else{
                Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
            }
        }

        routine_button.setOnClickListener {
            val db= DatabaseManager(view.context)
            var product_type = product.type.lowercase().replaceFirstChar { it.uppercase() }
            if(product.type.lowercase()=="mask"){
                if(product.area.lowercase()=="face")
                {
                    product_type="Mask"
                    val routine=db.getRoutineByType("face mask")
                    if(routine!=null)
                    {
                        val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                        Log.d("step",routine.id.toString()+" "+product_type.trim())
                        if(stepp!=null){
                            val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                            addProduct(new_step,view)
                        }
                        else{
                            val aux_step=Step(0,routine.id,0,"Mask","",0)
                            addStep(aux_step,view.context,"face mask",view, routine.id,product_type)

                        }
                    }
                    else{
                        Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(product.area.lowercase()=="eye")
                {
                    product_type="Mask"
                    val routine=db.getRoutineByType("eye mask")
                    if(routine!=null)
                    {
                        val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                        Log.d("step",routine.id.toString()+" "+product_type.trim())
                        if(stepp!=null){
                            val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                            addProduct(new_step,view)
                        }
                        else{
                            val aux_step=Step(0,routine.id,0,"Mask","",0)
                            addStep(aux_step,view.context,"eye mask",view, routine.id,product_type)

                        }
                    }
                    else{
                        Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(product.area.lowercase()=="lip")
                {
                    product_type="Mask"
                    val routine=db.getRoutineByType("lip mask")
                    if(routine!=null)
                    {
                        val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                        Log.d("step",routine.id.toString()+" "+product_type.trim())
                        if(stepp!=null){
                            val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                            addProduct(new_step,view)
                        }
                        else{
                            val aux_step=Step(0,routine.id,0,"Mask","",0)
                            addStep(aux_step,view.context,"lip mask",view, routine.id,product_type)

                        }
                    }
                    else{
                        Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(product.type.lowercase()=="exfoliator" || product.type.lowercase()=="peel"){
                val routine=db.getRoutineByType("exfoliation")
                if(routine!=null)
                {
                    val stepp=db.getStepByRoutineAndType(routine.id,product_type.trim())
                    Log.d("step",routine.id.toString()+" "+product_type.trim())
                    if(stepp!=null){
                        val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                        addProduct(new_step,view)
                    }
                    else{
                        val aux_step=Step(0,routine.id,0,"Exfoliator","",0)
                        addStep(aux_step,view.context,"Exfoliation",view, routine.id,product_type)

                    }
                }
                else{
                    Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
                }
            }
            else if(product.type.lowercase()=="sunscreen"){
                val routine=db.getSpf()
                if(routine!=null)
                {
                   val spf= SpfRoutine(routine.id,routine.user_id,routine.start_time,routine.end_time,routine.interval_minutes,routine.active_days,product.id)
                    addProductSpf(spf,view)
                }
                else{
                    Toast.makeText(context, "Routine disabled. Enable in Edit Routines.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun addProduct(step:Step,view: View){
        val sharedPreferences =view.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.add_products_to_step("Bearer $token",step).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val auxx=response.body()

                                val db= DatabaseManager(view.context)
                                val product_id=step.product_id
                                val step_id=step.id
                                if(product_id!=null && step_id!=null)
                                {
                                    val succes=db.assignProductToStep(step_id,product_id)
                                    if(succes){
                                        Toast.makeText(context, "Product added.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                        }
                        else ->{
                            Toast.makeText(context, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
                }

            })}
    }

    fun addProductSpf(step: SpfRoutine, view: View){
        val sharedPreferences =view.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.add_products_to_spf("Bearer $token",step).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val auxx=response.body()

                                val db= DatabaseManager(view.context)
                                val product_id=step.product_id
                                val step_id=step.id
                                if(product_id!=null && step_id!=null)
                                {
                                    val succes=db.assignProductToSpfRoutine(step_id,product_id)
                                    if(succes){
                                        Toast.makeText(context, "Product added.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                        }
                        else ->{
                            Toast.makeText(context, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
                }

            })}
    }
    fun addStep(s:Step,context: Context,routine_type: String,view: View,routine_id:Int,product_type:String){
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

                                    val new_step=auxx
                                    val dbManager = DatabaseManager(context)
                                    dbManager.insertStepAtEnd(new_step)


                                    val existingSteps = dbManager.getRoutineByType(routine_type)
                                    if(existingSteps!=null)
                                    {
                                        existingSteps.steps.last<Step>()
                                        Log.d("StepsBeforeInsert", "Steps before insert: ${existingSteps.steps.map { it.step_name }}")
                                    }

                                    dbManager.insertStepAtEnd(new_step)
                                    val stepp=dbManager.getStepByRoutineAndType(routine_id,product_type)
                                    if(stepp!=null){
                                        val new_step= Step(stepp.id,stepp.routine_id,stepp.step_order,stepp.step_name,stepp.step_name,product.id)
                                        addProduct(new_step,view)
                                    }

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

    // În Kotlin
    private fun startPulseAnimation(button: Button) {
        val scaleXAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 1.1f, 1.0f)
        val scaleYAnimator = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 1.1f, 1.0f)

        scaleXAnimator.duration = 1000
        scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleXAnimator.repeatMode = ObjectAnimator.REVERSE

        scaleYAnimator.duration = 1000
        scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleYAnimator.repeatMode = ObjectAnimator.REVERSE

        scaleXAnimator.start()
        scaleYAnimator.start()
        button.setBackgroundColor(Color.parseColor("#FF1493"))
    }

    private fun setSelectedButton(selectedBtn: Button, otherBtn: Button) {
        selectedBtn.elevation = 8.dpToPx()
        selectedBtn.translationZ = 4.dpToPx()
        selectedBtn.setBackgroundColor(ContextCompat.getColor( requireContext(), R.color.dark_blue))

        otherBtn.elevation = 2.dpToPx()
        otherBtn.translationZ = 0f
        otherBtn.setBackgroundColor(ContextCompat.getColor( requireContext(), R.color.blue_pastel_dark))
    }

    fun Int.dpToPx(): Float {
        return this * resources.displayMetrics.density
    }
    
}

