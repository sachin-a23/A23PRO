package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhoneAuthManager
import com.example.ui.theme.*

@Composable
fun PinLockOverlay(
    isPinSet: Boolean,
    onSetPin: (String) -> Unit,
    onAttemptUnlock: (String) -> Boolean
) {
    val context = LocalContext.current

    var enteredPin by remember { mutableStateOf("") }
    var confirmPinStep by remember { mutableStateOf(false) }
    var firstEnteredPin by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.5.dp, CyberGoldPrimary), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.98f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isPinSet) Icons.Default.Lock else Icons.Default.Security,
                    contentDescription = "Lock",
                    tint = CyberGoldPrimary,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isPinSet) "A23 PRO SECURITY LOCK" else "CREATE NEW 4-DIGIT PIN",
                    color = CyberGoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        !isPinSet && !confirmPinStep -> "Set a 4-Digit Security PIN for your account"
                        !isPinSet && confirmPinStep -> "Confirm your 4-Digit Security PIN"
                        else -> "Enter 4-Digit Security PIN to Unlock"
                    },
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // PIN indicators
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) CyberGoldPrimary else Color(0xFF1A2230))
                                .border(BorderStroke(1.dp, CyberGoldOutline), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Keypad 1-9, C, 0, OK
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                fun handlePinSubmit(pin: String) {
                    if (!isPinSet) {
                        if (!confirmPinStep) {
                            firstEnteredPin = pin
                            confirmPinStep = true
                            enteredPin = ""
                            Toast.makeText(context, "Re-enter PIN to Confirm", Toast.LENGTH_SHORT).show()
                        } else {
                            if (pin == firstEnteredPin) {
                                onSetPin(pin)
                                Toast.makeText(context, "✅ Security PIN Set Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "❌ PINs do not match! Try again.", Toast.LENGTH_SHORT).show()
                                confirmPinStep = false
                                firstEnteredPin = ""
                                enteredPin = ""
                            }
                        }
                    } else {
                        if (!onAttemptUnlock(pin)) {
                            Toast.makeText(context, "❌ Incorrect PIN!", Toast.LENGTH_SHORT).show()
                            enteredPin = ""
                        }
                    }
                }

                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        row.forEach { key ->
                            Button(
                                onClick = {
                                    when (key) {
                                        "C" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        "OK" -> {
                                            if (enteredPin.length == 4) {
                                                handlePinSubmit(enteredPin)
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                if (enteredPin.length == 4) {
                                                    handlePinSubmit(enteredPin)
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("keypad_$key"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2230)),
                                border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = key,
                                    color = if (key == "OK") CyberNeonGreen else CyberTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isPinSet) {
                    Spacer(modifier = Modifier.height(10.dp))
                    var showForgotPinDialog by remember { mutableStateOf(false) }

                    TextButton(
                        onClick = { showForgotPinDialog = true },
                        modifier = Modifier.testTag("forgot_pin_btn")
                    ) {
                        Text(
                            text = "Forgot Security PIN?",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showForgotPinDialog) {
                        var resetPhoneInput by remember { mutableStateOf("") }
                        var resetOtpInput by remember { mutableStateOf("") }
                        var resetVerificationId by remember { mutableStateOf<String?>(null) }
                        var isResetOtpSent by remember { mutableStateOf(false) }
                        var isSendingResetOtp by remember { mutableStateOf(false) }
                        var isVerifyingResetOtp by remember { mutableStateOf(false) }
                        var resetStatusMsg by remember { mutableStateOf<String?>(null) }
                        var isBlocked by remember { mutableStateOf(PhoneAuthManager.isUserBlocked(context)) }
                        var blockedMinutes by remember { mutableStateOf(PhoneAuthManager.getRemainingBlockedMinutes(context)) }

                        AlertDialog(
                            onDismissRequest = { showForgotPinDialog = false },
                            containerColor = Color(0xFF0D1420),
                            title = {
                                Text("🔒 MANDATORY MOBILE OTP VERIFICATION", color = CyberGoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Text(
                                        text = "To reset your 4-Digit Security PIN, verify your phone via Firebase SMS OTP.",
                                        color = CyberTextSecondary,
                                        fontSize = 11.sp
                                    )

                                    if (isBlocked) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = Color(0xFF2B0F0F),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color.Red),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "⛔ Account blocked due to 3 wrong OTP attempts. Try again in $blockedMinutes min(s).",
                                                color = Color(0xFFFF8888),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = resetPhoneInput,
                                        onValueChange = { newValue -> resetPhoneInput = newValue.filter { c -> c.isDigit() }.take(10) },
                                        label = { Text("Mobile Number", color = CyberGoldOutline, fontSize = 11.sp) },
                                        singleLine = true,
                                        enabled = !isResetOtpSent && !isBlocked && !isSendingResetOtp,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberGoldPrimary,
                                            unfocusedBorderColor = CyberGoldOutline,
                                            focusedTextColor = CyberTextPrimary,
                                            unfocusedTextColor = CyberTextPrimary
                                        )
                                    )

                                    if (!isResetOtpSent) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                if (PhoneAuthManager.isUserBlocked(context)) {
                                                    isBlocked = true
                                                    blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                                    Toast.makeText(context, "⛔ Account blocked due to 3 wrong OTP attempts. Try again in $blockedMinutes mins.", Toast.LENGTH_LONG).show()
                                                    return@Button
                                                }

                                                if (resetPhoneInput.length == 10) {
                                                    isSendingResetOtp = true
                                                    resetStatusMsg = null
                                                    PhoneAuthManager.sendSmsOtp(
                                                        context = context,
                                                        phoneNumber = resetPhoneInput,
                                                        onCodeSent = { vid ->
                                                            isSendingResetOtp = false
                                                            resetVerificationId = vid
                                                            isResetOtpSent = true
                                                            resetStatusMsg = "📩 SMS OTP dispatched via Firebase Auth to +91 $resetPhoneInput"
                                                            Toast.makeText(context, "📩 Reset SMS OTP Sent", Toast.LENGTH_SHORT).show()
                                                        },
                                                        onVerificationCompleted = { credential ->
                                                            val code = credential.smsCode
                                                            if (code != null) {
                                                                resetOtpInput = code
                                                            }
                                                        },
                                                        onError = { error ->
                                                            isSendingResetOtp = false
                                                            isBlocked = PhoneAuthManager.isUserBlocked(context)
                                                            blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                                            resetStatusMsg = error
                                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                        }
                                                    )
                                                } else {
                                                    Toast.makeText(context, "Please enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = !isBlocked && !isSendingResetOtp,
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            if (isSendingResetOtp) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("SENDING SMS OTP...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Text("SEND SMS OTP VIA FIREBASE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = resetOtpInput,
                                            onValueChange = { newValue -> resetOtpInput = newValue.filter { c -> c.isDigit() }.take(6) },
                                            label = { Text("Enter 6-Digit SMS OTP", color = CyberGoldOutline, fontSize = 11.sp) },
                                            singleLine = true,
                                            enabled = !isBlocked && !isVerifyingResetOtp,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyberNeonGreen,
                                                unfocusedBorderColor = CyberGoldOutline,
                                                focusedTextColor = CyberTextPrimary,
                                                unfocusedTextColor = CyberTextPrimary
                                            )
                                        )
                                    }

                                    resetStatusMsg?.let { msg ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = msg,
                                            color = if (msg.contains("❌") || msg.contains("⛔")) Color(0xFFFF8888) else Color(0xFF00E5FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                if (isResetOtpSent) {
                                    Button(
                                        onClick = {
                                            if (PhoneAuthManager.isUserBlocked(context)) {
                                                isBlocked = true
                                                blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                                Toast.makeText(context, "⛔ Account blocked due to 3 wrong OTP attempts. Try again in $blockedMinutes mins.", Toast.LENGTH_LONG).show()
                                                return@Button
                                            }

                                            val input = resetOtpInput.trim()
                                            if (input.length != 6) {
                                                Toast.makeText(context, "Please enter 6-digit OTP received via SMS", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }

                                            isVerifyingResetOtp = true
                                            PhoneAuthManager.verifySmsOtp(
                                                context = context,
                                                verificationId = resetVerificationId,
                                                code = input,
                                                onSuccess = {
                                                    isVerifyingResetOtp = false
                                                    showForgotPinDialog = false
                                                    confirmPinStep = false
                                                    firstEnteredPin = ""
                                                    enteredPin = ""
                                                    // Clears existing PIN so user creates a NEW one
                                                    onSetPin("")
                                                    Toast.makeText(context, "✅ Firebase OTP Verified! Set your NEW 4-Digit Security PIN now.", Toast.LENGTH_LONG).show()
                                                },
                                                onError = { error ->
                                                    isVerifyingResetOtp = false
                                                    isBlocked = PhoneAuthManager.isUserBlocked(context)
                                                    blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                                    resetStatusMsg = error
                                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        enabled = !isBlocked && !isVerifyingResetOtp,
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen)
                                    ) {
                                        if (isVerifyingResetOtp) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("VERIFYING...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        } else {
                                            Text("VERIFY OTP & RESET PIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showForgotPinDialog = false }) {
                                    Text("Cancel", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
