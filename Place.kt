package com.aysegulyilmaz.kotlintravelmaps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Place(
    @ColumnInfo(name="name")
    var name:String,
    @ColumnInfo(name="latitude")
    var latitude:Double,
    @ColumnInfo(name="longitude")
    var longitude:Double): Serializable {
    //bz bunu save fonksiyonun içinde çağırdığımda name latitude longitude değerlerini biz vericez
    //ama id bilgisini biz vermek zorunda değiliz o yüzden constructor dışında alıyoruz
    @PrimaryKey(autoGenerate = true)//kendi kendinde değer ver diyoruz
    var id = 0
}