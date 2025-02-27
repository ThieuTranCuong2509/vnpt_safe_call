package vnpt.trust_call.handler.runtime

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import vnpt.trust_call.handler.RequesterDelegate

fun ComponentActivity.runtimePermissionRequester(
    requestedPermissions: Array<String>,
) = RuntimePermissionRequesterDelegate(
    activity = this,
    requestedPermissions = requestedPermissions,
)

fun ComponentActivity.runtimePermissionRequester(
    requestedPermission: String,
) = RuntimePermissionRequesterDelegate(
    activity = this,
    requestedPermissions = arrayOf(requestedPermission),
)

class RuntimePermissionRequesterDelegate(
    private val activity: ComponentActivity,
    private val requestedPermissions: Array<String>,
) : RequesterDelegate<RuntimePermissionRequester>(
    activity = activity,
) {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            if (value == null) {
                value = RuntimePermissionRequester(
                    activity = activity,
                    requestedPermissions = requestedPermissions,
                )
            }
        }
    }
}