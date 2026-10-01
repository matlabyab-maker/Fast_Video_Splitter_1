package com.fastvideosplitter.app

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private val scenes = mutableListOf<Scene>()
    private lateinit var adapter: SceneAdapter
    private lateinit var status: TextView
    private var videoUri: Uri? = null

    private val openVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            videoUri = uri
            try { contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            status.text = "Video selected: ${displayName(uri)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.txtStatus)
        adapter = SceneAdapter(scenes)
        val recycler = findViewById<RecyclerView>(R.id.sceneRecycler)
        recycler.layoutManager = GridLayoutManager(this, 4)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnOpen).setOnClickListener { openVideo.launch(arrayOf("video/*")) }
        findViewById<Button>(R.id.btnAnalyze).setOnClickListener { analyzeScenes() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            scenes.removeAll { it.selected }
            scenes.forEachIndexed { i, s -> s.selected = false }
            adapter.notifyDataSetChanged()
            status.text = "${scenes.size} scene(s) remaining"
        }
        findViewById<Button>(R.id.btnSplit).setOnClickListener {
            status.text = "Splitter: Scene / Time / Frame / custom points are reserved for the processing engine."
        }
        findViewById<Button>(R.id.btnMerge).setOnClickListener {
            status.text = "Merge/Join: output will preserve order; Stream Copy is the preferred mode when technically possible."
        }
    }

    private fun analyzeScenes() {
        scenes.clear()
        val count = 12
        for (i in 0 until count) {
            val start = i * 10_000L
            scenes.add(Scene(i + 1, start, start + 10_000L, human = false))
        }
        adapter.notifyDataSetChanged()
        status.text = "Initial scene analysis created ${scenes.size} provisional scenes. Real scene/human detection will be added to the processing engine."
    }

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) return c.getString(0)
        }
        return uri.lastPathSegment ?: "video"
    }
}
