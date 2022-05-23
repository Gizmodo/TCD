package com.shop.tcd.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PlantDetailDescription() {
    Text("Hello Compose")
}

@Composable
private fun PlantName(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.h5,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PlantNamePreview() {
    MaterialTheme {
        PlantName("Apple")
    }
}