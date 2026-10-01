package com.fastvideosplitter.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class SceneAdapter(private val scenes: MutableList<Scene>) :
    RecyclerView.Adapter<SceneAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val thumb: ImageView = v.findViewById(com.fastvideosplitter.app.R.id.sceneThumb)
        val info: TextView = v.findViewById(com.fastvideosplitter.app.R.id.sceneInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_scene, parent, false))

    override fun getItemCount() = scenes.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val s = scenes[position]
        holder.thumb.setBackgroundColor(Color.DKGRAY)
        val human = if (s.human) " (Humen)" else ""
        holder.info.text = "Scene ${s.number}$human\n${fmt(s.startMs)} - ${fmt(s.endMs)}  |  ${fmt(s.durationMs)}"
        holder.itemView.setBackgroundColor(if (s.selected) 0xFFE0E0E0.toInt() else Color.TRANSPARENT)
        holder.itemView.setOnClickListener {
            s.selected = !s.selected
            notifyItemChanged(position)
        }
    }

    private fun fmt(ms: Long): String {
        val total = ms / 1000
        return String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
    }
}
