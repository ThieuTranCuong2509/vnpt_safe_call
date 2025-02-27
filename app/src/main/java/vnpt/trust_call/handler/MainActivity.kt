@file:OptIn(ExperimentalMaterial3Api::class)

package vnpt_trust_call.handler

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vnpt.trust_call.handler.SettingsSnackbar
import vnpt.trust_call.handler.runtime.RuntimePermissionRequester
import vnpt.trust_call.handler.runtime.RuntimePermissionState
import vnpt.trust_call.handler.special.SpecialPermissionRequester
import vnpt.trust_call.handler.special.SpecialPermissionState
import vnpt.trust_call.handler.role.RoleRequester
import vnpt.trust_call.handler.role.RoleState

class MainActivity : ComponentActivity() {
    private lateinit var specialPermissionRequester: SpecialPermissionRequester
    private lateinit var runtimePermissionRequester: RuntimePermissionRequester
    private lateinit var roleRequester: RoleRequester

    private val specialPermission = Manifest.permission.SYSTEM_ALERT_WINDOW
    private val runtimePermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    private val rolePermission = RoleManager.ROLE_CALL_SCREENING

    private lateinit var isSpecialPermissionButtonVisible: MutableState<Boolean>
    private lateinit var isRuntimePermissionsButtonVisible: MutableState<Boolean>
    private lateinit var rolePermissionButtonVisible: MutableState<Boolean>

    private val onSpecialPermissionClick = {
        specialPermissionRequester.requestPermission {
            isSpecialPermissionButtonVisible.value = it.second == SpecialPermissionState.DENIED
        }
    }
    private val onRuntimePermissionsClick = {
        runtimePermissionRequester.requestPermissions {
            isRuntimePermissionsButtonVisible.value = runtimePermissionRequester.areAllPermissionsGranted().not()
            if (it.containsValue(RuntimePermissionState.PERMANENTLY_DENIED)) {
                val settingsOpeningSnackbar = SettingsSnackbar(activity = this, view = window.decorView)
                settingsOpeningSnackbar.showSnackbar(
                    text = "Bạn phải cấp quyền trong Cài đặt!",
                    actionName = "Cài đặt"
                )
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private val rolePermissionClick = {
        roleRequester.requestRole {
            rolePermissionButtonVisible.value = it.second == RoleState.DENIED
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC6),
                    background = Color(0xFFF2F2F2),
                    surface = Color.White,
                    onBackground = Color.Black
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ContentInitializer()
                }
            }
        }
        init()
    }
    @Suppress("NonSkippableComposable")
    @Composable
    private fun ContentInitializer() {
        isSpecialPermissionButtonVisible = remember { mutableStateOf(specialPermissionRequester.isPermissionGranted().not()) }
        isRuntimePermissionsButtonVisible = remember { mutableStateOf(runtimePermissionRequester.areAllPermissionsGranted().not()) }
        rolePermissionButtonVisible = remember {
            mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) roleRequester.isRoleGranted().not() else false)
        }
        val areAllPermissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            specialPermissionRequester.isPermissionGranted() &&
                    runtimePermissionRequester.areAllPermissionsGranted() &&
                    roleRequester.isRoleGranted()
        } else {
            specialPermissionRequester.isPermissionGranted() && runtimePermissionRequester.areAllPermissionsGranted()
        }
        MainScreen(
            areAllPermissionsGranted = areAllPermissionsGranted,
            onSpecialPermissionClick = onSpecialPermissionClick,
            isSpecialPermissionButtonVisible = isSpecialPermissionButtonVisible.value,
            onRuntimePermissionsClick = onRuntimePermissionsClick,
            isRuntimePermissionsButtonVisible = isRuntimePermissionsButtonVisible.value,
            rolePermissionClick = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) rolePermissionClick else null,
            rolePermissionButtonVisible = rolePermissionButtonVisible.value
        )
    }
    private fun init() {
        specialPermissionRequester = SpecialPermissionRequester(activity = this, requestedPermission = specialPermission)
        runtimePermissionRequester = RuntimePermissionRequester(activity = this, requestedPermissions = runtimePermissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleRequester = RoleRequester(activity = this, requestedRole = rolePermission)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    areAllPermissionsGranted: Boolean,
    onSpecialPermissionClick: () -> Unit,
    isSpecialPermissionButtonVisible: Boolean,
    onRuntimePermissionsClick: () -> Unit,
    isRuntimePermissionsButtonVisible: Boolean,
    rolePermissionClick: (() -> Unit)?,
    rolePermissionButtonVisible: Boolean
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản Lý Quyền", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (areAllPermissionsGranted) {
                        Text(
                            text = "Tất cả các quyền đã được cấp.\nHãy sử dụng ngay!",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    } else {
                        if (isSpecialPermissionButtonVisible) {
                            PermissionButton(onClick = onSpecialPermissionClick, text = "Xin quyền hiển thị trên ứng dụng khác")
                        }
                        if (isRuntimePermissionsButtonVisible) {
                            PermissionButton(onClick = onRuntimePermissionsClick, text = "Xin phép truy cập vào danh bạ")
                        }
                        rolePermissionClick?.let {
                            if (rolePermissionButtonVisible) {
                                PermissionButton(onClick = it, text = "Xin quyền làm ứng dụng nhận dạng người gọi và chặn cuộc gọi làm phiền")
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionButton(onClick: () -> Unit, text: String) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ContentPreview() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF2F2F2),
            surface = Color.White,
            onBackground = Color.Black
        )
    ) {
        MainScreen(
            areAllPermissionsGranted = false,
            onSpecialPermissionClick = {},
            isSpecialPermissionButtonVisible = true,
            onRuntimePermissionsClick = {},
            isRuntimePermissionsButtonVisible = true,
            rolePermissionClick = {},
            rolePermissionButtonVisible = true
        )
    }
}
