package com.example.weatherapp

import android.app.AlertDialog
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
    private lateinit var spinnerUnits : Spinner
    private lateinit var editTextCity : EditText
    private lateinit var favouriteManager: FavouriteManager
    private var city = "Lodz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        favouriteManager = FavouriteManager(this)
        favouriteManager.addFavoriteCity("Poznan")
        favouriteManager.addFavoriteCity("Kongo")


        editTextCity = findViewById(R.id.editTextCity)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)
        imageButtonRefresh = findViewById(R.id.imageButtonRefresh)
        imageButtonRefresh.setOnClickListener {
            fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
        }

        buttonFavorites = findViewById<ImageButton>(R.id.imageButtonFavorites)
        buttonFavorites.setOnClickListener {
            showFavoriteCitiesDialog()
        }


        editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                city = editTextCity.text.toString()
                fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
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

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

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

        fetchDataFromApi("metric", city)
    }


    private fun fetchDataFromApi(unit : String, city : String) {
        val weatherApi = WeatherApi(weatherViewModel, unit, city)
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
            fetchDataFromApi("metric", city)
            alertDialog.dismiss()
        }

        listViewFavoriteCities.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val cityName = favoriteCitiesArray[position]

            AlertDialog.Builder(this)
                .setTitle("Remove City")
                .setMessage("Do you want to remove $cityName from favorites?")
                .setPositiveButton("Yes") { _, _ ->
                    // Usuń miasto z listy ulubionych w SharedPreferences
                    favouriteManager.removeFavoriteCity(cityName)

                    // Zaktualizuj listę ulubionych miast w adapterze
                    favoriteCitiesArray.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton("No", null)
                .show()

            true // Zwróć true, aby uniemożliwić wywołanie zdarzenia onClick
        }


    }




}