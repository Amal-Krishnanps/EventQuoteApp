package com.eventquote.app.ui.screens.license

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventquote.app.utils.LicenseManager

@Composable
fun LicenseScreen(onActivated: () -> Unit) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "fadeIn"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A0050),
                        Color(0xFF3700B3),
                        Color(0xFF6200EA)
                    )
                )
            )
            .alpha(alpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // App Icon / Logo area
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EventNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "EventQuote Pro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Professional Event Quotation Manager",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(48.dp))

            // License card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Enter License Key",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        "Contact the developer to get your\nregistration key.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )

                    // Key input field
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            // Auto-format as XXXX-XXXX-XXXX-XXXX
                            val raw = it.uppercase().replace(Regex("[^A-Z0-9]"), "")
                            keyInput = buildString {
                                raw.take(16).forEachIndexed { i, c ->
                                    if (i > 0 && i % 4 == 0) append('-')
                                    append(c)
                                }
                            }
                            isError = false
                        },
                        label = { Text("License Key", color = Color.White.copy(0.7f)) },
                        placeholder = { Text("XXXX-XXXX-XXXX-XXXX", color = Color.White.copy(0.4f)) },
                        isError = isError,
                        supportingText = if (isError) {
                            { Text(errorMsg, color = Color(0xFFFF6B6B)) }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Key, null, tint = Color(0xFFFFB800))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB800),
                            unfocusedBorderColor = Color.White.copy(0.4f),
                            errorBorderColor = Color(0xFFFF6B6B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFB800)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // Activate button
                    Button(
                        onClick = {
                            if (keyInput.length < 19) {
                                isError = true
                                errorMsg = "Please enter the complete 16-character key"
                                return@Button
                            }
                            val success = LicenseManager.activate(context, keyInput)
                            if (success) {
                                isSuccess = true
                                onActivated()
                            } else {
                                isError = true
                                errorMsg = "Invalid license key. Please contact the developer."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB800),
                            contentColor = Color(0xFF1A0050)
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Activate App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "📧 Contact developer for your license key",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
