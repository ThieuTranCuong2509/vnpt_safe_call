package vnpt.trust_call.handler

import android.app.Activity
import android.provider.Settings
import android.view.View
import com.google.android.material.snackbar.Snackbar
import vnpt.trust_call.handler.core.common.SettingsOpener

class SettingsSnackbar(
    private val activity: Activity,
    private val view: View,
) {
    fun showSnackbar(
        text: String,
        actionName: String,
    ) = Snackbar.make(
        view,
        text,
        Snackbar.LENGTH_LONG,
    ).setAction(actionName) {
        SettingsOpener.openSettings(
            activity,
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        )
    }.show()
}
