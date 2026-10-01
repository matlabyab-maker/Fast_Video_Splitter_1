
package com.fastvideosplitter.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fastvideosplitter.app.databinding.ItemSceneBinding

class SceneAdapter(
    private val items: MutableList<Scene>,
    private val onClick: (Scene) -> Unit,
    private val onMove: (Int, Int) -> Unit
) : RecyclerView.Adapter<SceneAdapter.VH>() {

    inner class VH(val b: ItemSceneBinding): RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemSceneBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val s = items[pos]
        h.b.info.text = "Scene ${"%03d".format(s.id)}" +
                if (s.human) " (Humen)" else "" +
                "\n${fmt(s.startMs)} - ${fmt(s.endMs)}"
        h.b.thumb.setImageBitmap(s.thumbnail)
        h.b.select.isChecked = s.selected
        h.b.select.setOnClickListener { s.selected = h.b.select.isChecked; onClick(s) }
        h.b.root.setOnClickListener { onClick(s) }
        h.b.root.setOnLongClickListener {
            if (bindingAdapterPosition > 0) onMove(bindingAdapterPosition, bindingAdapterPosition - 1)
            true
        }
    }

    override fun getItemCount() = items.size

    private fun fmt(ms: Long): String {
        val sec = ms / 1000
        return "%02d:%02d".format(sec / 60, sec % 60)
    }
}
