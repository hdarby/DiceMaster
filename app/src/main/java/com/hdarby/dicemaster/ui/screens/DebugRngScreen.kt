package com.hdarby.dicemaster.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.viewmodel.DebugViewModel
import com.hdarby.dicemaster.viewmodel.DieStats
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugRngScreen(
    viewModel: DebugViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_debug_rng)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.dieStatsList) { stats ->
                DieStatsCard(stats)
            }
        }
    }
}

@Composable
fun DieStatsCard(stats: DieStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.label_die_distribution, stats.faces),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            DistributionChart(stats)

            Spacer(modifier = Modifier.height(16.dp))

            StatsGrid(stats)
        }
    }
}

@Composable
fun DistributionChart(stats: DieStats) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = stats.faces
        val barWidth = width / barCount
        val maxFreq = stats.frequencies.values.maxOrNull()?.toFloat() ?: 1f

        // Draw Bars
        for (i in 1..stats.faces) {
            val freq = stats.frequencies[i] ?: 0
            val barHeight = (freq / maxFreq) * height
            drawRect(
                color = primaryColor.copy(alpha = 0.6f),
                topLeft = Offset((i - 1) * barWidth, height - barHeight),
                size = Size(barWidth - 2f, barHeight)
            )
        }
    }
}

@Composable
fun StatsGrid(stats: DieStats) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.label_mean, "%.2f".format(stats.mean)), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.label_std_dev, "%.2f".format(stats.stdDev)), style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.label_min, stats.min), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.label_max, stats.max), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.label_rolls, stats.totalRolls), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
