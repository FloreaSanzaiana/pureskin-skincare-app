package com.example.myapplication2.ui
import android.content.Context
import androidx.core.content.ContextCompat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductsActivity : AppCompatActivity() ,  FilterBottomSheetFragment.FilterListener{
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var filteredProductList: MutableList<Product>
    private lateinit var displayedList: MutableList<Product>
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private var currentSearchQuery: String = ""
    private lateinit var removefilter: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.products_page)


        val sharedPreferences = getSharedPreferences("filter_prefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val stepName = intent.getStringExtra("step_name")
        val stepArea = intent.getStringExtra("step_area")
        val routine_name = intent.getStringExtra("routine_name")

        if (!stepName.isNullOrEmpty()) {
            sharedPreferences.edit()
                .putStringSet("productTypes", setOf(stepName.lowercase()))
                .commit()
        }
        if(!stepArea.isNullOrEmpty() && stepName.toString().lowercase()=="mask"){
            sharedPreferences.edit()
                .putStringSet("area", setOf(stepArea.lowercase()))
                .commit()
        }


        removefilter = findViewById(R.id.removeFilter)
        productList = mutableListOf()
        filteredProductList = mutableListOf()
        displayedList = mutableListOf()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_products

        val imageButton = findViewById<ImageButton>(R.id.imageButton2)

        imageButton.setOnClickListener {
            val filterFragment = FilterBottomSheetFragment()
            filterFragment.show(supportFragmentManager, filterFragment.tag)
        }
        removefilter.setOnClickListener {
            sharedPreferences.edit().clear().apply()

            getFilteredProductsFromApi()
            removefilter.visibility= View.GONE



        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    val intent = Intent(this, MainMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_products -> {

                    true
                }
                R.id.nav_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_recommendations -> {
                    val intent = Intent(this, RecommendationsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_diary -> {
                    val intent = Intent(this, DiaryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }



        searchView = findViewById(R.id.searchView)
        searchView.setQueryHint("Search product...")
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setHintTextColor(ContextCompat.getColor(this@ProductsActivity, R.color.grey))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                applyFiltersAndSearch()
                return true
            }
        })

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = ProgressBar.GONE
        recyclerView = findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        productAdapter = ProductAdapter(this,displayedList,routine_name.toString())
        recyclerView.adapter = productAdapter

        getFilteredProductsFromApi()
        applyFiltersAndSearch()//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }







    private fun getFilteredProductsFromApi2() {
        val apiService = RetrofitInstance.instance
        progressBar.visibility = ProgressBar.VISIBLE
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        val call = apiService.getFilteredProducts("Bearer $token")
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@ProductsActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    val products = response.body() ?: emptyList()
                    Log.d("API Response", "Produse primite: ${products.size}")

                    productList.clear()
                    productList.addAll(products)

                    filteredProductList.clear()
                    filteredProductList.addAll(productList)

                    applyFiltersAndSearch()

                    progressBar.visibility = ProgressBar.GONE
                } else {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this@ProductsActivity, "Eroare la încărcarea produselor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this@ProductsActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
            }
        })
    }}

    private fun getFilteredProductsFromApi() {
        progressBar.visibility = ProgressBar.VISIBLE
        val db= DatabaseManager(this)
        val products_from_db=db.getAllProducts()
        if(products_from_db!=null){
            productList.clear()
            productList.addAll(products_from_db)

            filteredProductList.clear()
            filteredProductList.addAll(productList)

            applyFiltersAndSearch()

            progressBar.visibility = ProgressBar.GONE
        }
        else{
            progressBar.visibility = ProgressBar.GONE
            Toast.makeText(this@ProductsActivity, "Eroare la încărcarea produselor", Toast.LENGTH_SHORT).show()
        }
       }


    private fun applyFilters() {
        val sharedPreferences = getSharedPreferences("filter_prefs", MODE_PRIVATE)

        val hasSpf = sharedPreferences.getBoolean("hasSpf", false)
        val selectedAreas = sharedPreferences.getStringSet("area", emptySet()) ?: emptySet()
        val selectedTimes = sharedPreferences.getStringSet("timeOfDay", emptySet()) ?: emptySet()
        val selectedTypes = sharedPreferences.getStringSet("productTypes", emptySet()) ?: emptySet()



        filteredProductList.clear()

        val hasAnyFilter = hasSpf || selectedAreas.isNotEmpty() ||
                selectedTimes.isNotEmpty() || selectedTypes.isNotEmpty()

        if (!hasAnyFilter ) {
            filteredProductList.addAll(productList)
            return
        }
        removefilter.visibility= View.VISIBLE

        for (product in productList) {
            var shouldInclude = true

                if (hasSpf && product.spf <= 0) {
                    shouldInclude = false
                }
                if (selectedAreas.isNotEmpty() && !selectedAreas.contains(product.area.lowercase())) {
                    shouldInclude = false
                }
                if (selectedTimes.isNotEmpty() && !selectedTimes.contains(product.time.lowercase())) {
                    shouldInclude = false
                }
                if (selectedTypes.isNotEmpty() && !selectedTypes.contains(product.type.lowercase())) {
                    shouldInclude = false
                }

            if (shouldInclude) {
                filteredProductList.add(product)
            }
        }


    }


    private fun applySearch() {
        displayedList.clear()

        if (currentSearchQuery.isEmpty()) {
            displayedList.addAll(filteredProductList)
        } else {
            val lowerCaseQuery = currentSearchQuery.lowercase()

            for (product in filteredProductList) {
                if (product.name.lowercase().contains(lowerCaseQuery)) {
                    displayedList.add(product)
                }
            }
        }

        val productsNumber: TextView = findViewById(R.id.productsNumber)
        productsNumber.text = "${displayedList.size} Products"

        productAdapter.notifyDataSetChanged()
    }


    private fun applyFiltersAndSearch() {
        applyFilters()
        applySearch()
    }

    override fun onFiltersApplied() {
        applyFiltersAndSearch()

        val sp = getSharedPreferences("filter_prefs_routine_details", MODE_PRIVATE)
        sp.edit().clear().apply()

    }
}