package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.IgnoredAppEntity
import com.example.data.ReminderRepository
import com.example.data.SleepScheduleEntity
import com.example.data.UsageLogEntity
import com.example.model.AppItem
import com.example.model.ReminderSettings
import com.example.model.UsageState
import com.example.service.ContinuousUsageService
import com.example.service.CurrentAppDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScreenBreakViewModel(
    private val repository: ReminderRepository,
    private val appContext: Context
) : ViewModel() {

    val usageState: StateFlow<UsageState> = ContinuousUsageService.usageState
    val settings: StateFlow<ReminderSettings> = repository.settings
    val recentLogs: StateFlow<List<UsageLogEntity>> = repository.recentLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val reminderCount: StateFlow<Int> = repository.reminderCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val suppressedCount: StateFlow<Int> = repository.suppressedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val sleepSchedules: StateFlow<List<SleepScheduleEntity>> = repository.sleepSchedules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _rawInstalledApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterIgnoredOnly = MutableStateFlow(false)
    val filterIgnoredOnly: StateFlow<Boolean> = _filterIgnoredOnly.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _hasUsageAccess = MutableStateFlow(CurrentAppDetector.hasUsageAccess(appContext))
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(CurrentAppDetector.hasOverlayPermission(appContext))
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    // Combined filtered installed apps
    val installedApps: StateFlow<List<AppItem>> = combine(
        _rawInstalledApps,
        _searchQuery,
        _filterIgnoredOnly,
        repository.ignoredApps
    ) { apps, query, filterIgnored, ignoredEntities ->
        val ignoredMap = ignoredEntities.associate { it.packageName to it.isIgnored }
        val updatedApps = apps.map { app ->
            val isIgnored = ignoredMap[app.packageName] ?: false
            app.copy(isIgnored = isIgnored)
        }

        updatedApps.filter { app ->
            val matchesQuery = query.isEmpty() ||
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            val matchesFilter = !filterIgnored || app.isIgnored
            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<AppItem> { it.isIgnored }
                .thenBy { it.appName.lowercase() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadInstalledApps()
        viewModelScope.launch {
            repository.ensureDefaultScheduleIfEmpty()
        }
        // Auto-start service if not already running
        if (!_usageStateFlowIsRunning()) {
            ContinuousUsageService.startService(appContext)
        }
    }

    private fun _usageStateFlowIsRunning(): Boolean {
        return ContinuousUsageService.usageState.value.isServiceRunning
    }

    fun refreshPermissions() {
        _hasUsageAccess.value = CurrentAppDetector.hasUsageAccess(appContext)
        _hasOverlayPermission.value = CurrentAppDetector.hasOverlayPermission(appContext)
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            try {
                val apps = repository.loadInstalledLauncherApps()
                _rawInstalledApps.value = apps
            } catch (e: Exception) {
                // Ignore failure
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterIgnoredOnly(onlyIgnored: Boolean) {
        _filterIgnoredOnly.value = onlyIgnored
    }

    fun toggleAppIgnored(app: AppItem) {
        viewModelScope.launch {
            val newIgnoredState = !app.isIgnored
            repository.setAppIgnored(app.packageName, app.appName, newIgnoredState)
        }
    }

    fun toggleService() {
        val isRunning = usageState.value.isServiceRunning
        if (isRunning) {
            ContinuousUsageService.stopService(appContext)
        } else {
            ContinuousUsageService.startService(appContext)
        }
    }

    fun resetTimer() {
        val currentPkg = usageState.value.activePackage
        val currentName = usageState.value.activeAppName
        val intent = android.content.Intent(appContext, ContinuousUsageService::class.java).apply {
            action = ContinuousUsageService.ACTION_RESET
            if (currentPkg.isNotEmpty()) {
                putExtra(ContinuousUsageService.EXTRA_LOCKED_PACKAGE, currentPkg)
                putExtra(ContinuousUsageService.EXTRA_LOCKED_APP_NAME, currentName)
            }
        }
        appContext.startService(intent)
    }

    fun testReminderPopUp() {
        ContinuousUsageService.sendAction(appContext, ContinuousUsageService.ACTION_TEST_REMINDER)
    }

    fun updateInterval(minutes: Int) {
        repository.updateInterval(minutes)
    }

    fun updateSound(enabled: Boolean) {
        repository.updateSound(enabled)
    }

    fun updateVibration(enabled: Boolean) {
        repository.updateVibration(enabled)
    }

    fun updateOverlay(enabled: Boolean) {
        repository.updateOverlay(enabled)
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    val rawInstalledAppsList: List<AppItem>
        get() = _rawInstalledApps.value

    fun saveSchedule(schedule: SleepScheduleEntity) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: SleepScheduleEntity) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
        }
    }

    fun setScheduleEnabled(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setScheduleEnabled(id, isEnabled)
        }
    }

    class Factory(
        private val repository: ReminderRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScreenBreakViewModel(repository, context) as T
        }
    }
}
