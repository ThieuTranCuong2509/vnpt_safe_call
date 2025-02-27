package vnpt.trust_call.handler.core.role

import android.util.Log
import vnpt.trust_call.handler.core.common.TAG
import vnpt.trust_call.handler.role.RoleState

internal class RoleResultObserver {
    fun invoke(
        role: String,
        isGranted: Boolean,
    ) = role to when {
        isGranted -> {
            Log.d(TAG, "Role \"$role\" is granted")
            RoleState.GRANTED
        }

        else -> {
            Log.d(TAG, "Role \"$role\" is denied")
            RoleState.DENIED
        }
    }
}