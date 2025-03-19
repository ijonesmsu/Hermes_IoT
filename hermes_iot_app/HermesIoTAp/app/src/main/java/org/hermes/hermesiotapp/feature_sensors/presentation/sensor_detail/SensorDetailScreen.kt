package org.hermes.hermesiotapp.feature_sensors.presentation.sensor_detail

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.hermes.hermesiotapp.feature_sensors.presentation.sensor_detail.component.GraphicItem
import org.hermes.hermesiotapp.feature_sensors.presentation.sensor_detail.component.SensorDetailItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun SensorDetailScreen(
    viewModel: SensorDetailViewModel = hiltViewModel(),
    navController: NavController
) {

    val state = viewModel.state.value

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
        ) {

            if (!state.isLoading) {
                if (state.sensorRecord.isEmpty()) {
                    Text(
                        text = "No sensors found",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start
                    )
                    return@Column
                }

                Text(
                    text = "Return to ${state.sensorRecord.first().loc} sensors",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                )

                Text(text = state.sensorRecord.first().name, style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(8.dp))

                val x = state.sensorRecord.map { sensor ->
                    getMinuteOfTheDay(sensor.timestamp).toLong()
                }
                val y = state.sensorRecord.map { sensor -> sensor.value }
                GraphicItem(x, y)

                HorizontalDivider()

                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                ) {
                    items(state.sensorRecord) {sensor ->
                        SensorDetailItem(sensor)
                    }
                }

                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

private fun getMinuteOfTheDay(timestamp: Long): Int {

    val time = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)

    return time.hour * 3600 + time.minute * 60 + time.second
}