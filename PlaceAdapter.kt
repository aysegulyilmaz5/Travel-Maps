package com.aysegulyilmaz.kotlintravelmaps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aysegulyilmaz.kotlintravelmaps.databinding.RecyclerviewRowBinding
import com.aysegulyilmaz.kotlintravelmaps.model.Place
import com.aysegulyilmaz.kotlintravelmaps.view.MapsActivity
import kotlinx.coroutines.NonDisposableHandle.parent

//bütün görünümüzlerimizi sağlayan yardımcı sınıf
//placeadapter içerisinde placelist isteniyor
class PlaceAdapter(val placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){
    class PlaceHolder(val recyclerviewRowBinding: RecyclerviewRowBinding) : RecyclerView.ViewHolder(recyclerviewRowBinding.root){
    //placeholder bir görünüm vermek istiyor bu yüzden binding kullandık

    }
    //görünümümüzü bağlıyoruz
    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.recyclerviewRowBinding.recyclerViewTextView.text = placeList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
       val recyclerviewRowBinding = RecyclerviewRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(recyclerviewRowBinding)
    }
}