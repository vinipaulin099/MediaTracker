package com.betopompolo.mediatracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.betopompolo.mediatracker.data.MockCatalog
import com.betopompolo.mediatracker.databinding.ActivityHomeBinding
import com.betopompolo.mediatracker.databinding.HomeListItemLayoutBinding
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener { finish() }
        binding.listView.layoutManager = LinearLayoutManager(this)
        binding.listView.adapter = CatalogAdapter(MockCatalog.items) { item ->
            startActivity(Intent(this, BookDetailActivity::class.java).apply {
                putExtra(BookDetailActivity.MEDIA_ID_KEY, item.id)
            })
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val spacing = (16 * resources.displayMetrics.density).toInt()
            view.setPadding(bars.left + spacing, bars.top, bars.right + spacing, bars.bottom)
            insets
        }
    }
}

private class CatalogAdapter(private val items: List<MediaItem>, private val onOpen: (MediaItem) -> Unit) :
    RecyclerView.Adapter<CatalogAdapter.Holder>() {
    class Holder(val binding: HomeListItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        HomeListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            title.text = item.title
            pageCount.text = root.context.getString(R.string.catalog_units, item.totalUnits, item.kind.unit)
            badge.badgeText.text = item.kind.label
            cover.setImageResource(coverResource(item))
            cover.contentDescription = root.context.getString(R.string.cover_description, item.title)
            root.setOnClickListener { onOpen(item) }
        }
    }
}

internal fun coverResource(item: MediaItem): Int = when (item.id) {
    "book-lotr" -> R.drawable.lotr_book_cover
    "movie-blade-runner" -> R.drawable.blade_runner
    else -> R.drawable.ic_launcher_foreground
}
