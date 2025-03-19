package org.hermes.hermesiotapp.feature_sensors.presentation.sensor_detail

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.hermes.hermesiotapp.common.Constants
import org.hermes.hermesiotapp.feature_sensors.domain.use_case.get_sensor.GetSensorUseCase
import org.hermes.hermesiotapp.feature_sensors.domain.util.DataRefresher
import org.hermes.hermesiotapp.feature_sensors.domain.util.Resource
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SensorDetailViewModel @Inject constructor(
    private val getSensorUseCase: GetSensorUseCase,
    savedState: SavedStateHandle
): ViewModel() {

    private val _state = mutableStateOf(SensorDetailState())
    val state: State<SensorDetailState> get() = _state

    private val job = DataRefresher(
        refreshInterval = 15.seconds,
        scope = viewModelScope,
        onRefresh = { savedState.get<String>(Constants.PARAM_SENSOR_ID)?.let { sensorId -> getSensor(sensorId) } }
    ).invoke()


    init {
        savedState.get<String>(Constants.PARAM_SENSOR_ID)?.let { sensorId ->
            getSensor(sensorId)
            Log.d("SensorDetailViewModel", "Fetching data for sensor: $sensorId")
        }

        job.start()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    private fun getSensor(sensorId: String) {
        getSensorUseCase(sensorId).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = SensorDetailState(isLoading = true)

                is Resource.Success -> _state.value = SensorDetailState(sensorRecord = result.data ?: emptyList())

                is Resource.Error -> _state.value = SensorDetailState(error = result.message ?: "An unexpected error occurred")
            }
        }.launchIn(viewModelScope)
    }
}