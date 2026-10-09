package com.betopompolo.mediatracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.betopompolo.mediatracker.data.MockCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ViewsFlowRobolectricTest {
    @Test fun catalogClickSendsTheActualSelectedId() {
        val controller = Robolectric.buildActivity(HomeActivity::class.java).setup()
        try {
            val activity = controller.get()
            val list = activity.findViewById<RecyclerView>(R.id.list_view)
            @Suppress("UNCHECKED_CAST")
            val adapter = list.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
            val holder = adapter.createViewHolder(list, 0)
            adapter.bindViewHolder(holder, 1)
            holder.itemView.performClick()
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(BookDetailActivity::class.java.name, intent.component?.className)
            assertEquals(MockCatalog.items[1].id, intent.getStringExtra(BookDetailActivity.MEDIA_ID_KEY))
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun optionalFieldsAndProgressAreRestoredInXmlDetails() {
        val intent = Intent(RuntimeEnvironment.getApplication(), BookDetailActivity::class.java)
            .putExtra(BookDetailActivity.MEDIA_ID_KEY, "movie-independent")
        val controller = Robolectric.buildActivity(BookDetailActivity::class.java, intent).setup()
        val activity = controller.get()
        assertEquals("Filme independente", activity.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("Não informado", activity.findViewById<TextView>(R.id.creator).text.toString())
        activity.findViewById<Button>(R.id.next_button).performClick()
        assertEquals("1 de 90 minutos", activity.findViewById<TextView>(R.id.progress_text).text.toString())
        val saved = Bundle()
        controller.saveInstanceState(saved).pause().stop().destroy()
        val restored = Robolectric.buildActivity(BookDetailActivity::class.java, intent)
            .create(saved).start().resume().visible()
        try {
            assertEquals("1 de 90 minutos", restored.get().findViewById<TextView>(R.id.progress_text).text.toString())
        } finally { restored.pause().stop().destroy() }
    }

    @Test fun unknownIdShowsAnEmptyStateInsteadOfCrashing() {
        val intent = Intent(RuntimeEnvironment.getApplication(), BookDetailActivity::class.java)
            .putExtra(BookDetailActivity.MEDIA_ID_KEY, "missing")
        val controller = Robolectric.buildActivity(BookDetailActivity::class.java, intent).setup()
        try {
            assertEquals(View.VISIBLE, controller.get().findViewById<View>(R.id.not_found).visibility)
            assertEquals(View.GONE, controller.get().findViewById<View>(R.id.detail_content).visibility)
        } finally { controller.pause().stop().destroy() }
    }
}
