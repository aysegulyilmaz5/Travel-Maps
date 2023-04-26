package com.aysegulyilmaz.kotlintravelmaps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.aysegulyilmaz.kotlintravelmaps.R
import com.aysegulyilmaz.kotlintravelmaps.adapter.PlaceAdapter
import com.aysegulyilmaz.kotlintravelmaps.databinding.ActivityMainBinding
import com.aysegulyilmaz.kotlintravelmaps.model.Place
import com.aysegulyilmaz.kotlintravelmaps.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
        val placeDao = db.placeDao()

        compositeDisposable.add(
            placeDao.getAll()// bu bize liste veriyor(listofplaces)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    //daonun içinde artık places döndürüyorz bu yüzden handleresponse places paramteresini almalı
    private fun handleResponse(placeList : List<Place>){
        binding.recyclerVew.layoutManager = LinearLayoutManager(this)
        val adapter = PlaceAdapter(placeList)
        binding.recyclerVew.adapter = adapter

    }

    //oncreateoptionsmenu menüyü ana aktivitemize bağlıyoruz
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater //menüler ile çalışırken enü inflater oluşturmamız lazım
        menuInflater.inflate(R.menu.place_menu,menu) //oluşturduğumuz menüyü paramtere olan menü ile bağlıyoruz
        return super.onCreateOptionsMenu(menu)
    }
    //onoptionsıtemselected o menüden bir şey seçilirse ne olacak
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_place){
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}