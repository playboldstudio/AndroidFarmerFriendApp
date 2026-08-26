package com.example.androidfarmerfriend.util

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/** Banner states surfaced by [InAppUpdateHelper]. */
sealed interface UpdateBanner {
    data object Available : UpdateBanner
    data object Downloading : UpdateBanner
    data object ReadyToInstall : UpdateBanner
}

/**
 * Flexible in-app update flow: check on resume, download in the background,
 * prompt for restart when ready. UI renders only; logic lives here.
 */
class InAppUpdateHelper(private val activity: Activity) {

    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var pendingUpdate: AppUpdateInfo? = null

    var banner by mutableStateOf<UpdateBanner?>(null)
        private set

    private val stateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> banner = UpdateBanner.Downloading
            InstallStatus.DOWNLOADED -> banner = UpdateBanner.ReadyToInstall
            InstallStatus.INSTALLED, InstallStatus.CANCELED, InstallStatus.FAILED -> {
                if (state.installStatus() != InstallStatus.INSTALLED) checkForUpdate()
                else banner = null
            }
        }
    }

    init {
        manager.registerListener(stateListener)
    }

    /** Call from onResume so returning users re-check availability. */
    fun onResume() {
        when (banner) {
            is UpdateBanner.ReadyToInstall -> return
            else -> checkForUpdate()
        }
    }

    fun onPause() = Unit

    fun startDownload() {
        val info = pendingUpdate ?: return
        manager.startUpdateFlowForResult(
            info,
            AppUpdateType.FLEXIBLE,
            activity,
            UPDATE_REQUEST_CODE
        )
    }

    fun completeUpdate() = manager.completeUpdate()

    fun dismiss() {
        banner = null
    }

    private fun checkForUpdate() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            when (info.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        pendingUpdate = info
                        if (banner == null) banner = UpdateBanner.Available
                    }
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // A flexible download was interrupted by the system — resume it.
                    pendingUpdate = info
                    if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        startDownload()
                    }
                }
                else -> banner = null
            }
        }.addOnFailureListener { banner = null }
    }

    companion object {
        const val UPDATE_REQUEST_CODE = 4711
    }
}
