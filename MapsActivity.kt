package com.aysegulyilmaz.kotlintravelmaps.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.aysegulyilmaz.kotlintravelmaps.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.aysegulyilmaz.kotlintravelmaps.databinding.ActivityMapsBinding
import com.aysegulyilmaz.kotlintravelmaps.model.Place
import com.aysegulyilmaz.kotlintravelmaps.roomdb.PlaceDao
import com.aysegulyilmaz.kotlintravelmaps.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener { //kullanıcı uzun tıklama yapıp yer seçsin diye {

private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    var trackBoolean: Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao: PlaceDao
    val compositeDisposable = CompositeDisposable()
    var placeFromMain : Place? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.aysegulyilmaz.kotlintravelmaps",
            MODE_PRIVATE)
        trackBoolean = false //eğer false ise bir defaya mahsus çalıştır değilse çalıştırma
        selectedLatitude = 0.0
        selectedLongitude = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            //.allowMainThreadQueries() //doğru yöntem değil doğrusu rxjava
            .build()

        placeDao = db.placeDao()

        binding.saveBtn.isEnabled = false //uzun tıklanıp yer belirlemeyince save button çalışmıyor
    }

    //Location manager konum yöneticisi kounmla ilgili tüm detayları alıyor
    //location listener konumda değişiklik olursa haber veren arayüz
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener (this) // bunu aktif kullandığımız haritada kullanacağımızı belirtmek için buraya gelip yazmamız gerekiyor harita açıldığında etkin olsun diye
        //bunu yapmazsak harita ile listener arasında bağlantıyı kuramıyoruz
        //casting
        val intent = intent
        val info = intent.getStringExtra("info")
        if(info == "new"){
            binding.saveBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager //Nasıl bir servis olduğunu söylemek için casting yaptık diğer türlü hangi işlemi yapacağını bilmiyor
            locationListener = object : LocationListener{
                override fun onLocationChanged(location: Location) {
                    //burda konum değişikliğini istediğimiz zaman alabiliyoruz
                    //bu konum daha önce kayıtlı mı shared preferences ile almaya çalışıcaz
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)//default değer eğer öyle bir şey kayıtlı değilse ne vereyim diyor ilk defa çalıştırdığımızda
                    //bu değer kayıtlı olmayacak bu yüzden false diyoruz
                    if(trackBoolean == false){
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                        /*ilk defa uygulama açılıyor ve trackboolean diye bir değişken var değeri false
                        shared preferncesa bakıyoruz bu anahtar kelimeyle kaydedilmiş değer var mı diye
                        yok diyoruz false olsun eğer false ise bunu çalıştır sonra da truaya çevir diyoruz
                        trueya çevrildikten sonra onlocationchange bir daha çağırılıyor tekrar sharedpreferncestan değer alınıyıor
                        ve bakıyoruz artık true o yüzden burası bir daha çalışmıyor böylece onlocationchange bir defa
                        çağırılacak
                         */


                    }

                }

            }

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                    Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

                    }.show()//legth indeinite ile kullanıcı aksiyon alana kadar kalıyor
                else{
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }else{
                //permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)//minimum saniye 1000 salise 1 saniye uzaklık 10 metre dedik
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation != null){
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled = true //konumumu mu etkinleştirdin mi demek


            }

            //latitude,longitude
            //lat->48.85391 lon->2.2913515

            /* val eifell = LatLng(48.85391,2.2913515)
             mMap.addMarker(MarkerOptions().position(eifell).title("Eifell Tower"))
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eifell,10f))//zoom seviyesi 0 ile 25 arasında değişiyor*/

        }else{
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place

            placeFromMain?.let {
                val latlng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                binding.placeText.setText(it.name)
                binding.saveBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.VISIBLE
            }

        }

    }

    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)//minimum saniye 1000 salise 1 saniye uzaklık 10 metre dedik
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(lastLocation != null){
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    mMap.isMyLocationEnabled = true //konumumu mu etkinleştirdin mi demek
                    //bunu yapınca konumuz mavi daire olarak gözüküyor ve hangi yöne baktığımızda gözüküyor
                    //kullanıcının nerede olduğunu shared preferences kullanmadan sadece bu satırı yazarakta kullanabiliriz

                }

                //permission granted
            }
            else{
                //permission denied
                Toast.makeText(this@MapsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }

        } //boolean değer veriyor evet izin verildi hayır izin verilmedi gibi

    }

    override fun onMapLongClick(p0: LatLng) {
        //uzun tıklanınca ne olacak fonksiyonu
        mMap.clear() //daha önce işaretlenmiş bir marker varsa onu silmemiz lazım
        mMap.addMarker(MarkerOptions().position(p0)) //zaten bize fonksiyon içinde latlong değeri verilmiş sadece oraya marker ekliyoruz
        //kullanıcı her uzun tıkladığında farklı bir yeri işaretliyor bunları kaydetmek için selectedlatitude ve selected longitude variablelarını oluşturup
        //onların içine atıyoruz
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
        binding.saveBtn.isEnabled = true


    }

    fun save(view : View){

        if(selectedLatitude != null && selectedLongitude != null) {

            val place =
                Place(binding.placeText.text.toString(), selectedLatitude!!, selectedLongitude!!)
                compositeDisposable.add(
                    placeDao.insert(place)
                        .subscribeOn(Schedulers.io())//bir yerde bu işlmeler yapılacak bir yerde gözlemnlenecek işlmeler için subscribe on kullanıyoruz
                        //bunları io threadde yaptığımızı göstermek için scheduler.io() kullandık. schedulers rxjavadan gelen bir sınıf io threade ulaşmamızı sağlıyor
                        .observeOn(AndroidSchedulers.mainThread())//sonucu nerede kullanacağımızı yazıyoruz
                        /*neden biri schedulers digeri androidschedulers?
                            çünkü rxjava java için yazılmış bir kütüphane ve mainthread sadece androidde mevcut
                        **/
                        .subscribe(this::handleResponse)//işlem bittikten sonra ne olacağını söylüyoruz
                        //bunu da ayrı bir fonksiyonda yapıp buraya vermemiz gerekiyor
                )       //handlerepsonse a parantez koymuyoruz çünkü handleresponse u burds çalıştır demiyoruz sadece referans veriyoruz
        }
    }

    private fun handleResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//açık olan flagları kapatıyor
        startActivity(intent)
    }
    fun delete(view:View){
        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}