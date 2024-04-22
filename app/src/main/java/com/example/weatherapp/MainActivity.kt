package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var imageButtonRefresh: ImageButton
    private lateinit var buttonFavorites: ImageButton
    private lateinit var buttonAddFavorites: ImageButton
    private lateinit var spinnerUnits: Spinner
    private lateinit var editTextCity: EditText
    private lateinit var favouriteManager: FavouriteManager
    lateinit var city: String
    private lateinit var weatherApi: WeatherApi
    private lateinit var networkUtils: NetworkUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        favouriteManager = FavouriteManager(this)
        networkUtils = NetworkUtils(this)

        city = favouriteManager.getFavoriteCities().firstOrNull() ?: "Warsaw"
        editTextCity = findViewById(R.id.editTextCity)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)
        imageButtonRefresh = findViewById(R.id.imageButtonRefresh)
        imageButtonRefresh.setOnClickListener {
            fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
        }

        val unitOptions = resources.getStringArray(R.array.unit_options)

        val adapterSpinner = ArrayAdapter(this, R.layout.custom_spinner_item, unitOptions)
        adapterSpinner.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerUnits.adapter = adapterSpinner

        buttonFavorites = findViewById(R.id.imageButtonFavorites)
        buttonFavorites.setOnClickListener {
            showFavoriteCitiesDialog()
        }

        buttonAddFavorites = findViewById(R.id.imageButtonAddFavorites)
        favouriteManager.setFavourite(favouriteManager, city, buttonAddFavorites)


        buttonAddFavorites.setOnClickListener {
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
                favouriteManager.setFavourite(favouriteManager, city, buttonAddFavorites)
                hideKeyboard()
                editTextCity.clearFocus()
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

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        if (networkUtils.isNetworkAvailable()) {
            fetchDataFromApi("metric", city)
        } else {
            favouriteManager.getWeatherData(city)?.let { weatherViewModel.setWeatherData(it) }
            Toast.makeText(this, "Dane mogą być nieaktualne. Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
        startDataRefreshThread(this)

    }

    fun fetchDataFromApi(unit: String, city: String) {
        weatherApi = WeatherApi(this, weatherViewModel, unit, city, favouriteManager)
        weatherApi.execute()
    }

    private fun convertItemToUnit(item: String): String {
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

        // Pobranie przycisku Close i zmiana jego wyglądu
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white)) // Ustawienie koloru tekstu
        positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue)) // Ustawienie koloru tła

        listViewFavoriteCities.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                city = favoriteCitiesArray[position]
                favouriteManager.setFavourite(favouriteManager, city, buttonAddFavorites)
                if (networkUtils.isNetworkAvailable()) {
                    fetchDataFromApi("metric", city)
                } else {
                    favouriteManager.getWeatherData(city)
                        ?.let { weatherViewModel.setWeatherData(it) }
                    Toast.makeText(this, "Dane mogą być nieaktualne. Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss()
            }
    }

    private fun startDataRefreshThread(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val refreshIntervalMillis = 60 * 1000L

        val refreshRunnable = object : Runnable {
            override fun run() {
                if(networkUtils.isNetworkAvailable()) {
                    fetchDataFromApi("metric", city)
                    Toast.makeText(context, "Zaaktualizowano dane", Toast.LENGTH_SHORT).show()
                    handler.postDelayed(this, refreshIntervalMillis)
                }
            }
        }
        handler.post(refreshRunnable)

    }

    override fun onStop() {
        super.onStop()
        favouriteManager.saveWeatherDataForFavouriteCities(favouriteManager, this)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextCity.windowToken, 0)
    }

}