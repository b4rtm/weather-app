package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.fragments.AdditionalInfoFragment
import com.example.weatherapp.fragments.TodayFragment
import com.example.weatherapp.fragments.WeekForecastFragment
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
    lateinit var networkUtils: NetworkUtils
    private var refreshThread: Thread? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("city", city)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        favouriteManager = FavouriteManager(this)
        networkUtils = NetworkUtils(this)


        city = savedInstanceState?.getString("city")
            ?: (favouriteManager.getFavoriteCities().firstOrNull() ?: "Warsaw")
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
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Remove from Favorites")
                builder.setMessage("Do you want to remove this city from favorites?")
                builder.setPositiveButton("Yes") { dialog, _ ->
                    buttonAddFavorites.setImageResource(R.drawable.favorite)
                    favouriteManager.removeFavoriteCity(city)
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                buttonAddFavorites.setImageResource(R.drawable.favorite_gold)
                favouriteManager.addFavoriteCity(city)
            }
        }


        editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                fetchDataFromApi(
                    convertItemToUnit(spinnerUnits.selectedItem.toString()),
                    editTextCity.text.toString()
                )
                favouriteManager.setFavourite(
                    favouriteManager,
                    editTextCity.text.toString(),
                    buttonAddFavorites
                )
                hideKeyboard()
                editTextCity.clearFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)
        if (isTablet()) {
            setupTabletLayout()
        } else {
            setupPhoneLayout()
        }

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
            fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
        } else {
            favouriteManager.getWeatherData(city)?.let { weatherViewModel.setWeatherData(it) }
            Toast.makeText(
                this,
                "Data could be out of date. No Internet connection",
                Toast.LENGTH_SHORT
            ).show()
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
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(
            ContextCompat.getColor(this, R.color.white))
        positiveButton.setBackgroundColor(
            ContextCompat.getColor(this, R.color.blue))

        listViewFavoriteCities.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                city = favoriteCitiesArray[position]
                favouriteManager.setFavourite(favouriteManager, city, buttonAddFavorites)
                if (networkUtils.isNetworkAvailable()) {
                    fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
                } else {
                    favouriteManager.getWeatherData(city)
                        ?.let { weatherViewModel.setWeatherData(it) }
                }
                alertDialog.dismiss()
            }
    }

    private fun startDataRefreshThread(context: Context) {
        refreshThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                if (networkUtils.isNetworkAvailable()) {
                    fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
                    runOnUiThread {
                        Toast.makeText(context, "Data updated", Toast.LENGTH_SHORT).show()
                    }
                }
                try {
                    Thread.sleep(30 * 1000L)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
        refreshThread?.start()
    }

    private fun stopDataRefreshThread() {
        refreshThread?.interrupt()
        refreshThread = null
    }

    override fun onStop() {
        super.onStop()
        stopDataRefreshThread()
        favouriteManager.saveWeatherDataForFavouriteCities(favouriteManager, this)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextCity.windowToken, 0)
    }

    private fun isTablet(): Boolean {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return screenWidthDp >= 600
    }

    private fun setupTabletLayout() {
        val fragmentManager = supportFragmentManager

        val todayFragment = TodayFragment()
        val infoFragment = AdditionalInfoFragment()

        val fragmentTransaction1 = fragmentManager.beginTransaction()
        fragmentTransaction1.replace(R.id.todayFragmentContainer, todayFragment)
        fragmentTransaction1.commit()

        val fragmentTransaction2 = fragmentManager.beginTransaction()
        fragmentTransaction2.replace(R.id.infoFragmentContainer, infoFragment)
        fragmentTransaction2.commit()

        val weekFragment = WeekForecastFragment()
        val fragmentTransaction3 = fragmentManager.beginTransaction()
        fragmentTransaction3.replace(R.id.weekFragmentContainer, weekFragment)
        fragmentTransaction3.commit()
    }

    private fun setupPhoneLayout() {
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)
        viewPager2.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("More info"))
        tabLayout.addTab(tabLayout.newTab().setText("Today"))
        tabLayout.addTab(tabLayout.newTab().setText("Week"))

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

}