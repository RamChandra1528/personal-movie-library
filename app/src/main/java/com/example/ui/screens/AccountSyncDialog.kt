package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CloudSearchRecord
import com.example.data.SyncStatus
import com.example.ui.components.CineVaultLogo
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGoldBright
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountSyncDialog(
    currentUser: FirebaseUser?,
    syncStatus: SyncStatus,
    cloudSearchHistory: List<CloudSearchRecord>,
    isSigningIn: Boolean,
    authError: String?,
    onSignInWithGoogle: (android.content.Context) -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit = { _, _ -> },
    onRegisterWithEmail: (email: String, pass: String) -> Unit = { _, _ -> },
    onResetPassword: (email: String) -> Unit = {},
    onSignOut: () -> Unit,
    onForceSync: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedAuthMethod by remember { mutableIntStateOf(0) } // 0 = Email, 1 = Google
    var isRegisterMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .testTag("account_sync_dialog")
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CineCardBorder, RoundedCornerShape(20.dp)),
            color = CineSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with custom logo and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CineVaultLogo(sizeDp = 36)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Personal Movie Library",
                                color = CineTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cloud Vault & Multi-Device Sync",
                                color = CineGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("close_account_dialog")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CineTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (currentUser != null) {
                    // ==================== SIGNED IN VIEW ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CineSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(CineGold, CineCyan))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser.displayName ?: currentUser.email ?: "U").take(1).uppercase(),
                                    color = CineDarkBg,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.displayName ?: (currentUser.email?.substringBefore("@") ?: "Member"),
                                    color = CineTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentUser.email ?: "Authenticated Account",
                                    color = CineTextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CineGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cloud Synchronized",
                                        color = CineGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Account identifiers & security
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CineDarkBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = CineGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Account UID",
                                        color = CineTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = currentUser.uid.take(12) + "...",
                                    color = CineTextTertiary,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CineSurfaceVariant, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = CineCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cloud Status",
                                        color = CineTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                if (syncStatus.isSyncing) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = CineGold,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Syncing...",
                                            color = CineGold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    val lastTimeStr = syncStatus.lastSyncedTime?.let {
                                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                        "Updated ${sdf.format(Date(it))}"
                                    } ?: "Up to date"
                                    Text(
                                        text = lastTimeStr,
                                        color = CineTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Cloud search history snippet
                    if (cloudSearchHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Recently Synced Searches",
                            color = CineGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cloudSearchHistory.take(3).forEach { record ->
                                Surface(
                                    color = CineSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = CineTextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = record.query,
                                            color = CineTextPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onForceSync,
                            modifier = Modifier
                                .testTag("force_sync_button")
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineGold,
                                contentColor = CineDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .testTag("sign_out_button")
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF6B6B)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign Out",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // ==================== SIGNED OUT VIEW ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(CineSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = CineGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Backup & Restore Your Collection",
                            color = CineTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sign in to keep your watch history, ratings, and notes safe across devices and reinstalls.",
                            color = CineTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tab selector: Email & Password vs Google Sign-In
                        TabRow(
                            selectedTabIndex = selectedAuthMethod,
                            containerColor = CineDarkBg,
                            contentColor = CineGold,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedAuthMethod]),
                                    color = CineGold
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = selectedAuthMethod == 0,
                                onClick = { selectedAuthMethod = 0 },
                                text = { Text("Email Login", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = selectedAuthMethod == 1,
                                onClick = { selectedAuthMethod = 1 },
                                text = { Text("Google Sign-In", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedAuthMethod == 0) {
                            // EMAIL & PASSWORD FORM
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CineDarkBg, RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isRegisterMode) CineSurfaceVariant else Color.Transparent)
                                        .clickable { isRegisterMode = false }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Log In",
                                        color = if (!isRegisterMode) CineGold else CineTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isRegisterMode) CineSurfaceVariant else Color.Transparent)
                                        .clickable { isRegisterMode = true }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account",
                                        color = if (isRegisterMode) CineGold else CineTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                modifier = Modifier
                                    .testTag("email_input")
                                    .fillMaxWidth(),
                                label = { Text("Email Address", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = CineGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CineGold,
                                    unfocusedBorderColor = CineCardBorder,
                                    focusedTextColor = CineTextPrimary,
                                    unfocusedTextColor = CineTextPrimary,
                                    cursorColor = CineGold
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                modifier = Modifier
                                    .testTag("password_input")
                                    .fillMaxWidth(),
                                label = { Text("Password", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = CineGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = CineTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (isRegisterMode) {
                                            onRegisterWithEmail(emailInput, passwordInput)
                                        } else {
                                            onSignInWithEmail(emailInput, passwordInput)
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CineGold,
                                    unfocusedBorderColor = CineCardBorder,
                                    focusedTextColor = CineTextPrimary,
                                    unfocusedTextColor = CineTextPrimary,
                                    cursorColor = CineGold
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (!isRegisterMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onResetPassword(emailInput) }
                                    ) {
                                        Text(
                                            text = "Forgot password?",
                                            color = CineGold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (isRegisterMode) {
                                        onRegisterWithEmail(emailInput, passwordInput)
                                    } else {
                                        onSignInWithEmail(emailInput, passwordInput)
                                    }
                                },
                                modifier = Modifier
                                    .testTag("email_auth_button")
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CineGold,
                                    contentColor = CineDarkBg
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = CineDarkBg,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isRegisterMode) "Creating account..." else "Authenticating...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        text = if (isRegisterMode) "Create Account & Sync" else "Sign In to Vault",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            // GOOGLE SIGN-IN TAB
                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { onSignInWithGoogle(context) },
                                modifier = Modifier
                                    .testTag("google_sign_in_button")
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F1F1F)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = CineDarkBg,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Connecting to Google...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF4285F4)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Sign in with Google",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F1F1F)
                                        )
                                    }
                                }
                            }
                        }

                        // Auth Error
                        if (authError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = authError,
                                color = Color(0xFFFF6B6B),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Features list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CineDarkBg, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureRow(
                                icon = Icons.Default.Devices,
                                iconTint = CineCyan,
                                title = "Seamless Device Sync",
                                description = "Access your watchlist and ratings anywhere."
                            )
                            FeatureRow(
                                icon = Icons.Default.Security,
                                iconTint = CineGreen,
                                title = "Reinstall Protection",
                                description = "Uninstalling never loses your catalog."
                            )
                            FeatureRow(
                                icon = Icons.Default.Lock,
                                iconTint = CineGold,
                                title = "Private & Secure Vault",
                                description = "Zero-trust encrypted database rules."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = CineTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = CineTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
