package com.betopompolo.mediatracker

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.betopompolo.mediatracker.data.MockCatalog
import com.betopompolo.mediatracker.databinding.BadgeLayoutBinding
import com.betopompolo.mediatracker.databinding.BookDetailLayoutBinding
import com.betopompolo.mediatracker.model.MediaItem

// Segunda tela da etapa XML. O nome da Activity foi mantido da base das aulas.
class BookDetailActivity : AppCompatActivity() {
    companion object { const val MEDIA_ID_KEY = "media_id" }
    private lateinit var binding: BookDetailLayoutBinding
    private var item: MediaItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = BookDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener { finish() }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        item = MockCatalog.find(intent.getStringExtra(MEDIA_ID_KEY))?.let { original ->
            original.copy(currentUnits = (savedInstanceState?.getInt("progress") ?: 0).coerceIn(0, original.totalUnits))
        }
        val selected = item
        if (selected == null) {
            binding.detailContent.visibility = View.GONE
            binding.notFound.visibility = View.VISIBLE
            return
        }
        binding.title.text = selected.title
        binding.cover.setImageResource(coverResource(selected))
        binding.cover.contentDescription = getString(R.string.cover_description, selected.title)
        binding.synopsis.text = selected.synopsis ?: getString(R.string.no_synopsis)
        binding.creator.text = selected.creator ?: getString(R.string.not_informed)
        binding.year.text = selected.year?.toString() ?: getString(R.string.not_informed)
        listOf(selected.kind.label, "${selected.totalUnits} ${selected.kind.unit}").forEach { text ->
            val badge = BadgeLayoutBinding.inflate(layoutInflater, binding.badgeContainer, false)
            badge.badgeText.text = text
            binding.badgeContainer.addView(badge.root)
        }
        binding.previousButton.setOnClickListener { changeProgress(-1) }
        binding.nextButton.setOnClickListener { changeProgress(1) }
        binding.completeButton.setOnClickListener { item = item?.copy(currentUnits = selected.totalUnits); renderProgress() }
        renderProgress()
    }

    private fun changeProgress(delta: Int) {
        item = item?.let { it.copy(currentUnits = (it.currentUnits + delta).coerceIn(0, it.totalUnits)) }
        renderProgress()
    }

    private fun renderProgress() {
        val current = item ?: return
        binding.progressText.text = getString(R.string.session_progress, current.currentUnits, current.totalUnits, current.kind.unit)
        binding.progressPercent.text = getString(R.string.progress_percent, current.progressPercent)
        binding.progressIndicator.progress = current.progressPercent
        binding.previousButton.isEnabled = current.currentUnits > 0
        binding.nextButton.isEnabled = current.currentUnits < current.totalUnits
    }

    override fun onSaveInstanceState(outState: Bundle) {
        item?.let { outState.putInt("progress", it.currentUnits) }
        super.onSaveInstanceState(outState)
    }
}
