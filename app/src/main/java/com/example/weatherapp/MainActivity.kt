package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var weatherViewModel : WeatherViewModel
    private lateinit var imageButtonRefresh : ImageButton
    private lateinit var buttonFavorites : ImageButton
    private lateinit var buttonAddFavorites : ImageButton
    private lateinit var spinnerUnits : Spinner
    private lateinit var editTextCity : EditText
    private lateinit var favouriteManager: FavouriteManager
    lateinit var city : String
    private lateinit var weatherApi : WeatherApi



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        favouriteManager = FavouriteManager(this)

        favouriteManager.removeFavoriteCity("lodz")
        favouriteManager.removeFavoriteCity("turek")
        favouriteManager.removeFavoriteCity("Poznan")

        city = favouriteManager.getFavoriteCities().firstOrNull() ?: "Warsaw"
        editTextCity = findViewById(R.id.editTextCity)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)
        imageButtonRefresh = findViewById(R.id.imageButtonRefresh)
        imageButtonRefresh.setOnClickListener {
        fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
        }

        buttonFavorites = findViewById(R.id.imageButtonFavorites)
        buttonFavorites.setOnClickListener {
            showFavoriteCitiesDialog()
        }

        buttonAddFavorites = findViewById(R.id.imageButtonAddFavorites)
        setFavourite()

        buttonAddFavorites.setOnClickListener{
            val isFavorite = favouriteManager.isCityFavorite(city)
            if (isFavorite) {
                buttonAddFavorites.setImageResource(R.drawable.favorite)
                favouriteManager.removeFavoriteCity(city)
            } else {
                buttonAddFavorites.setImageResource(R.drawable.favorite_gold)
                favouriteManager.addFavoriteCity(city)
            }
        }


        editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                city = editTextCity.text.toString()
                fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
                setFavourite()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

        tabLayout.addTab(tabLayout.newTab().setText("More info"))
        tabLayout.addTab(tabLayout.newTab().setText("Today"))
        tabLayout.addTab(tabLayout.newTab().setText("Week"))

        viewPager2.adapter = adapter
        viewPager2.setCurrentItem(1, false)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0 != null) {
                    viewPager2.currentItem = p0.position
                }
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })

        spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = convertItemToUnit(parent?.getItemAtPosition(position).toString())

                fetchDataFromApi(selectedUnit, city)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        if (isNetworkAvailable()) {
            fetchDataFromApi("metric", city)
        }
        else{
            favouriteManager.getWeatherData(city)?.let { weatherViewModel.setWeatherData(it) }
        }
    }

    private fun setFavourite() {
        val isFavorite = favouriteManager.isCityFavorite(city)
        if (isFavorite) {
            buttonAddFavorites.setImageResource(R.drawable.favorite_gold)
        } else {
            buttonAddFavorites.setImageResource(R.drawable.favorite)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }


    private fun fetchDataFromApi(unit : String, city : String) {
        weatherApi = WeatherApi(this, weatherViewModel, unit, city, favouriteManager)
        weatherApi.execute()
    }



    private fun convertItemToUnit(item : String): String {
        return when (item) {
            "Celsius" -> "metric"
            "Fahrenheit" -> "imperial"
            else -> "standard"
        }
    }

    private fun showFavoriteCitiesDialog() {
        val favoriteCities = favouriteManager.getFavoriteCities()
        val favoriteCitiesArray = favoriteCities.toMutableList()

        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_favorite_cities, null)
        val listViewFavoriteCities = dialogView.findViewById<ListView>(R.id.listViewFavoriteCities)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, favoriteCitiesArray)
        listViewFavoriteCities.adapter = adapter

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        listViewFavoriteCities.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            city = favoriteCitiesArray[position]
            setFavourite()
            if (isNetworkAvailable()) {
                fetchDataFromApi("metric", city)
            }
            else{
                favouriteManager.getWeatherData(city)?.let { weatherViewModel.setWeatherData(it) }
            }
            alertDialog.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        saveWeatherDataForFavouriteCities()
    }

    private fun saveWeatherDataForFavouriteCities() {
        val favouriteCities = favouriteManager.getFavoriteCities()
        for (cityItem in favouriteCities) {
            fetchDataFromApi("metric", cityItem)
//            Log.d("xdd", weatherViewModel.getWeatherData().value.toString())
//            weatherViewModel.getWeatherData().value?.let {
//                favouriteManager.saveWeatherData(cityItem,
//                    it
//                )
//            }
        }
    }
}