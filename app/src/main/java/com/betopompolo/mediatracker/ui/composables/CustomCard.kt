package com.betopompolo.mediatracker.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.betopompolo.mediatracker.ui.theme.MediaTrackerTheme

@Composable
fun CustomCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            content()
        }
    }
}

@Preview
@Composable
private fun CustomCardPreview() {
    MediaTrackerTheme {
        CustomCard("Test 2") {
            Text("Hello world!")
        }
    }
}
