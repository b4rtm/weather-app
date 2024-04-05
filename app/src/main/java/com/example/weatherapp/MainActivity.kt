package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var  spinnerUnits : Spinner
    private lateinit var editTextCity : EditText
    private var city = "Lodz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextCity = findViewById(R.id.editTextCity)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)
        imageButtonRefresh = findViewById(R.id.imageButtonRefresh)
        imageButtonRefresh.setOnClickListener {
            fetchDataFromApi(convertItemToUnit(spinnerUnits.selectedItem.toString()), city)
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



}