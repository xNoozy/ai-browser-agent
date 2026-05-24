package com.qalab.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalab.launcher.domain.model.DeviceProfile
import com.qalab.launcher.domain.model.NetworkType
import com.qalab.launcher.domain.model.Orientation
import com.qalab.launcher.domain.repository.DeviceProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceLabViewModel @Inject constructor(
    private val deviceProfileRepository: DeviceProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceLabUiState())
    val uiState: StateFlow<DeviceLabUiState> = _uiState

    init {
        loadProfiles()
        initializeDefaultProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            deviceProfileRepository.getAllProfiles().collect { profiles ->
                _uiState.value = _uiState.value.copy(profiles = profiles)
            }
        }
    }

    private fun initializeDefaultProfiles() {
        viewModelScope.launch {
            val defaultProfiles = listOf(
                DeviceProfile(
                    id = "pixel-7",
                    name = "Pixel 7",
                    manufacturer = "Google",
                    model = "Pixel 7",
                    screenWidthDp = 412,
                    screenHeightDp = 915,
                    densityDpi = 420,
                    sdkVersion = 34,
                    isDefault = true
                ),
                DeviceProfile(
                    id = "samsung-s23",
                    name = "Galaxy S23",
                    manufacturer = "Samsung",
                    model = "SM-S911B",
                    screenWidthDp = 360,
                    screenHeightDp = 780,
                    densityDpi = 425,
                    sdkVersion = 34
                ),
                DeviceProfile(
                    id = "pixel-tablet",
                    name = "Pixel Tablet",
                    manufacturer = "Google",
                    model = "Pixel Tablet",
                    screenWidthDp = 1280,
                    screenHeightDp = 800,
                    densityDpi = 276,
                    sdkVersion = 34,
                    orientation = Orientation.LANDSCAPE
                ),
                DeviceProfile(
                    id = "small-phone",
                    name = "Small Phone (Budget)",
                    manufacturer = "Generic",
                    model = "Budget Phone",
                    screenWidthDp = 320,
                    screenHeightDp = 568,
                    densityDpi = 320,
                    sdkVersion = 28,
                    networkType = NetworkType.HSPA
                ),
                DeviceProfile(
                    id = "fold-open",
                    name = "Galaxy Z Fold (Open)",
                    manufacturer = "Samsung",
                    model = "SM-F946B",
                    screenWidthDp = 884,
                    screenHeightDp = 2176,
                    densityDpi = 373,
                    sdkVersion = 34
                )
            )

            defaultProfiles.forEach { profile ->
                if (deviceProfileRepository.getById(profile.id) == null) {
                    deviceProfileRepository.createProfile(profile)
                }
            }
        }
    }

    fun selectProfile(profile: DeviceProfile) {
        _uiState.value = _uiState.value.copy(selectedProfile = profile)
    }

    fun showAddProfileDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun createProfile(profile: DeviceProfile) {
        viewModelScope.launch {
            deviceProfileRepository.createProfile(profile)
            _uiState.value = _uiState.value.copy(showAddDialog = false)
        }
    }
}

data class DeviceLabUiState(
    val profiles: List<DeviceProfile> = emptyList(),
    val selectedProfile: DeviceProfile? = null,
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = false
)
