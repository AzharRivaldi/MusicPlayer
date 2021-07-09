package com.azhar.mymusic.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.mymusic.R
import com.azhar.mymusic.activities.PlaySongActivity
import com.azhar.mymusic.adapter.MainAdapter.MyViewHolder
import kotlinx.android.synthetic.main.list_item_main.view.*
import java.util.*

/**
 * Created by Azhar Rivaldi on 26-06-2021
 * Youtube Channel : https://bit.ly/2PJMowZ
 * Github : https://github.com/AzharRivaldi
 * Twitter : https://twitter.com/azharrvldi_
 * Instagram : https://www.instagram.com/azhardvls_
 * Linkedin : https://www.linkedin.com/in/azhar-rivaldi
 */

class MainAdapter(var songList: ArrayList<HashMap<String, String>>, var context: Context) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = songList[position]["songTitle"].replace("_", " ")
        holder.cvListMusic.setOnClickListener {
            val intent = Intent(context, PlaySongActivity::class.java)
            intent.putExtra("songIndex", songList[position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListMusic: CardView
        var textView: TextView

        init {
            cvListMusic = itemView.cvListMusic
            textView = itemView.tvJudulLagu
        }
    }

}