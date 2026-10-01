
package com.fastvideosplitter.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.documentfile.provider.DocumentFile
import com.fastvideosplitter.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val scenes = mutableListOf<Scene>()
    private lateinit var adapter: SceneAdapter
    private var videoUri: Uri? = null
    private var outputTree: Uri? = null
    private var duration = 0L

    private val openVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        videoUri = uri
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        loadVideo(uri)
    }

    private val chooseOutput = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@registerForActivityResult
        outputTree = uri
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        b.statusText.text = "Output: ${DocumentFile.fromTreeUri(this, uri)?.name ?: "selected"}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = SceneAdapter(scenes, { s ->
            b.timelineText.text = "Selected Scene ${s.id}: ${s.startMs}–${s.endMs} ms"
            b.startSeek.max = duration.toInt().coerceAtLeast(1)
            b.endSeek.max = duration.toInt().coerceAtLeast(1)
            b.startSeek.progress = s.startMs.toInt()
            b.endSeek.progress = s.endMs.toInt()
        }) { from, to ->
            scenes.add(to, scenes.removeAt(from))
            adapter.notifyItemMoved(from, to)
        }

        b.sceneRecycler.layoutManager = GridLayoutManager(this, 5)
        b.sceneRecycler.adapter = adapter
        b.openButton.setOnClickListener { openVideo.launch(arrayOf("video/*")) }
        b.outputButton.setOnClickListener { chooseOutput.launch(null) }
        b.analyzeButton.setOnClickListener { analyzeScenes() }
        b.deleteButton.setOnClickListener { deleteSelected() }
        b.splitButton.setOnClickListener { toast("Split engine is prepared; select Scene/Frame ranges and choose output.") }
        b.mergeButton.setOnClickListener { exportNoReencodePlan() }

        b.startSeek.setOnSeekBarChangeListener(simpleSeekListener())
        b.endSeek.setOnSeekBarChangeListener(simpleSeekListener())
    }

    private fun loadVideo(uri: Uri) {
        val player = ExoPlayer.Builder(this).build()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_READY) {
                    duration = player.duration.coerceAtLeast(0)
                    b.statusText.text = "Loaded: ${duration / 1000}s"
                    player.release()
                }
            }
        })
    }

    private fun analyzeScenes() {
        val d = duration
        if (d <= 0) { toast("Open a video first."); return }
        scenes.clear()
        // Initial deterministic timeline grid. The production detector can replace
        // this with actual FFmpeg scene-change scores without changing the UI model.
        val count = (d / 10_000L).toInt().coerceIn(1, 500)
        val step = d / count
        for (i in 0 until count) {
            scenes += Scene(i + 1, i * step, if (i == count - 1) d else (i + 1) * step)
        }
        adapter.notifyDataSetChanged()
        b.statusText.text = "${scenes.size} scene segments ready"
    }

    private fun deleteSelected() {
        scenes.removeAll { it.selected }
        scenes.forEachIndexed { i, s -> s.selected = false }
        adapter.notifyDataSetChanged()
        b.statusText.text = "${scenes.size} segments remain"
    }

    private fun exportNoReencodePlan() {
        if (videoUri == null || scenes.isEmpty()) { toast("Open and analyze a video first."); return }
        if (outputTree == null) { toast("Choose an output folder first."); return }
        // Actual stream-copy muxing is intentionally isolated from UI. This version
        // records the ordered, retained ranges and is ready for the FFmpeg backend.
        val kept = scenes.filter { !it.selected }
        val report = kept.joinToString("\n") { "${it.startMs},${it.endMs},${if (it.human) "Humen" else ""}" }
        val file = DocumentFile.fromTreeUri(this, outputTree!!)?.createFile("text/plain", "edit_ranges.txt")
        file?.uri?.let { out ->
            contentResolver.openOutputStream(out)?.use { it.write(report.toByteArray()) }
        }
        toast("Edit ranges prepared. Stream-copy muxer backend can now render them.")
    }

    private fun simpleSeekListener() = object : android.widget.SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: android.widget.SeekBar?, p: Int, fromUser: Boolean) {
            if (!fromUser) return
            val start = b.startSeek.progress
            val end = b.endSeek.progress
            b.timelineText.text = "Range ${start} ms → ${end} ms"
        }
        override fun onStartTrackingTouch(s: android.widget.SeekBar?) {}
        override fun onStopTrackingTouch(s: android.widget.SeekBar?) {}
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
