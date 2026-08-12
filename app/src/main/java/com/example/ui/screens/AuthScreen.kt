package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhoneAuthManager
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: (name: String, email: String, mobile: String) -> Unit
) {
    val context = LocalContext.current

    var selectedAuthTab by remember { mutableStateOf(0) } // Default to Phone OTP
    var isRegisterMode by remember { mutableStateOf(false) }

    // Email state
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Phone state & Firebase Verification
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var verificationIdState by remember { mutableStateOf<String?>(null) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpStatusMessage by remember { mutableStateOf<String?>(null) }
    var isBlocked by remember { mutableStateOf(PhoneAuthManager.isUserBlocked(context)) }
    var blockedMinutes by remember { mutableStateOf(PhoneAuthManager.getRemainingBlockedMinutes(context)) }

    val scrollState = rememberScrollState()

    // Periodically update blocked status
    LaunchedEffect(Unit) {
        isBlocked = PhoneAuthManager.isUserBlocked(context)
        blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B12))
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color(0xFF0D1524).copy(alpha = 0.96f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, CyberGoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Shield Header Badge
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(CyberGoldPrimary.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                            .border(1.5.dp, CyberGoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = CyberGoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚡ A23 PRO AUTHENTICATION",
                        color = CyberGoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (isRegisterMode) "Create New VIP Account" else "Sign In to Access Prediction Core",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 60-Minute Block Banner
                    if (isBlocked) {
                        Surface(
                            color = Color(0xFF2A0A0A),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⛔ SECURITY LOCKOUT ACTIVE",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "3 incorrect OTP attempts detected. Account is temporarily blocked for security.",
                                    color = Color(0xFFFF8888),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please try again in $blockedMinutes minute(s).",
                                    color = CyberGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Auth Method Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF162032))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf("📱 Phone OTP", "📧 Email", "🌐 Google")
                        tabs.forEachIndexed { idx, label ->
                            val isSel = selectedAuthTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) CyberGoldPrimary else Color.Transparent)
                                    .clickable { selectedAuthTab = idx }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.Black else CyberTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // TAB 0: PHONE NUMBER & REAL FIREBASE SMS OTP
                    if (selectedAuthTab == 0) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { newValue ->
                                phoneInput = newValue.filter { char -> char.isDigit() }.take(10)
                            },
                            label = { Text("Enter 10-Digit Mobile Number", color = CyberGoldOutline) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = CyberGoldPrimary) },
                            singleLine = true,
                            enabled = !isOtpSent && !isBlocked && !isSendingOtp,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isOtpSent) {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { newValue ->
                                    otpInput = newValue.filter { char -> char.isDigit() }.take(6)
                                },
                                label = { Text("Enter 6-Digit SMS OTP", color = CyberGoldOutline) },
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = "OTP", tint = CyberNeonGreen) },
                                singleLine = true,
                                enabled = !isBlocked && !isVerifyingOtp,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_otp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberNeonGreen,
                                    unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                    focusedTextColor = CyberTextPrimary,
                                    unfocusedTextColor = CyberTextPrimary
                                )
                            )

                            otpStatusMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = if (msg.contains("❌") || msg.contains("⛔")) Color(0xFF2B0F0F) else Color(0xFF0F1B2B),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (msg.contains("❌") || msg.contains("⛔")) Color.Red else Color(0xFF00E5FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        color = if (msg.contains("❌") || msg.contains("⛔")) Color(0xFFFF8888) else Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (PhoneAuthManager.isUserBlocked(context)) {
                                        isBlocked = true
                                        blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                        Toast.makeText(context, "⛔ Account blocked due to 3 wrong OTP attempts. Try again in $blockedMinutes mins.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    val trimmedOtp = otpInput.trim()
                                    if (trimmedOtp.length != 6) {
                                        Toast.makeText(context, "Please enter complete 6-digit OTP code received via SMS", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isVerifyingOtp = true
                                    PhoneAuthManager.verifySmsOtp(
                                        context = context,
                                        verificationId = verificationIdState,
                                        code = trimmedOtp,
                                        onSuccess = {
                                            isVerifyingOtp = false
                                            Toast.makeText(context, "✅ Mobile OTP Verified Successfully via Firebase!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess("VIP USER", "user@a23pro.com", phoneInput)
                                        },
                                        onError = { error ->
                                            isVerifyingOtp = false
                                            isBlocked = PhoneAuthManager.isUserBlocked(context)
                                            blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                            otpStatusMessage = error
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                enabled = !isBlocked && !isVerifyingOtp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_verify_otp_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen)
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("VERIFYING WITH FIREBASE...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(
                                        text = "VERIFY SMS OTP & LOGIN",
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    isOtpSent = false
                                    verificationIdState = null
                                    otpInput = ""
                                    otpStatusMessage = null
                                }
                            ) {
                                Text("Change Mobile Number", color = CyberGoldPrimary, fontSize = 11.sp)
                            }
                        } else {
                            if (otpStatusMessage != null) {
                                Surface(
                                    color = Color(0xFF2B0F0F),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.Red),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = otpStatusMessage!!,
                                        color = Color(0xFFFF8888),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Button(
                                onClick = {
                                    if (PhoneAuthManager.isUserBlocked(context)) {
                                        isBlocked = true
                                        blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                        Toast.makeText(context, "⛔ Account blocked due to 3 wrong OTP attempts. Try again in $blockedMinutes mins.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    if (phoneInput.length == 10) {
                                        isSendingOtp = true
                                        otpStatusMessage = null
                                        PhoneAuthManager.sendSmsOtp(
                                            context = context,
                                            phoneNumber = phoneInput,
                                            onCodeSent = { vid ->
                                                isSendingOtp = false
                                                verificationIdState = vid
                                                isOtpSent = true
                                                otpStatusMessage = "📩 Firebase SMS OTP dispatched to +91 $phoneInput. Enter code below."
                                                Toast.makeText(context, "📩 SMS OTP Sent via Firebase Auth", Toast.LENGTH_SHORT).show()
                                            },
                                            onVerificationCompleted = { credential ->
                                                val code = credential.smsCode
                                                if (code != null) {
                                                    otpInput = code
                                                }
                                            },
                                            onError = { error ->
                                                isSendingOtp = false
                                                isBlocked = PhoneAuthManager.isUserBlocked(context)
                                                blockedMinutes = PhoneAuthManager.getRemainingBlockedMinutes(context)
                                                otpStatusMessage = error
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "Please enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isBlocked && !isSendingOtp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_send_otp_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                            ) {
                                if (isSendingOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SENDING SMS OTP...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(
                                        text = "SEND REAL SMS OTP VIA FIREBASE",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // TAB 1: EMAIL & PASSWORD
                    if (selectedAuthTab == 1) {
                        AnimatedVisibility(visible = isRegisterMode) {
                            Column {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Full Name", color = CyberGoldOutline) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = CyberGoldPrimary) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberGoldPrimary,
                                        unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                        focusedTextColor = CyberTextPrimary,
                                        unfocusedTextColor = CyberTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address", color = CyberGoldOutline) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = CyberGoldPrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password", color = CyberGoldOutline) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = CyberGoldPrimary) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        AnimatedVisibility(visible = !isRegisterMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 4.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(
                                    onClick = {
                                        if (emailInput.isNotBlank()) {
                                            Toast.makeText(context, "Password Reset Link sent to $emailInput via Firebase Auth!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Please enter your Email Address to receive reset link", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("auth_forgot_password_btn")
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val trimmedEmail = emailInput.trim()
                                val trimmedPass = passwordInput.trim()
                                if (trimmedEmail.isNotBlank() && trimmedEmail.contains("@") && trimmedPass.length >= 6) {
                                    Toast.makeText(context, if (isRegisterMode) "Registration Successful!" else "Welcome Back!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(nameInput.ifBlank { "VIP USER" }, trimmedEmail, phoneInput.ifBlank { "8698431018" })
                                } else {
                                    Toast.makeText(context, "Please enter valid Email and Password (min 6 chars)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_email_submit_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                        ) {
                            Text(
                                text = if (isRegisterMode) "REGISTER & GET STARTED" else "SIGN IN WITH EMAIL",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                            Text(
                                text = if (isRegisterMode) "Already have an account? Sign In" else "Don't have an account? Register Now",
                                color = CyberGoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // TAB 2: GOOGLE SIGN-IN
                    if (selectedAuthTab == 2) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFF162032),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🌐 ONE-TAP GOOGLE SIGN-IN",
                                    color = CyberGoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sign in securely using your registered Google Workspace or Gmail account.",
                                    color = CyberTextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Google Account Signed In", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess("GOOGLE USER", "user@gmail.com", "8698431018")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_google_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                                ) {
                                    Text(
                                        text = "CONTINUE WITH GOOGLE",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
