package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppLogo
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthTab
import com.example.ui.viewmodel.EBloodViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: EBloodViewModel? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_loading")

    // Pulsing Scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Pulsing Alpha
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Shimmering Gradient Shift
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2563EB),
            Color(0xFF60A5FA),
            Color(0xFF93C5FD),
            Color(0xFF2563EB)
        ),
        start = Offset(shimmerOffset, shimmerOffset),
        end = Offset(shimmerOffset + 400f, shimmerOffset + 400f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // ONLY the app name "EBloodDonation" with glowing loading animation
        Text(
            text = "EBloodDonation",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            style = TextStyle(
                brush = gradientBrush
            ),
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        )
    }
}

@Composable
fun GoogleGLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF4285F4)
        )
    }
}

@Composable
fun AuthScreen(viewModel: EBloodViewModel) {
    val context = LocalContext.current
    val email by viewModel.inputEmail.collectAsStateWithLifecycle()
    val password by viewModel.inputPassword.collectAsStateWithLifecycle()
    val phone by viewModel.inputPhone.collectAsStateWithLifecycle()
    val name by viewModel.inputName.collectAsStateWithLifecycle()
    val isSignUp by viewModel.isSignUpMode.collectAsStateWithLifecycle()
    val passwordVisible by viewModel.passwordVisible.collectAsStateWithLifecycle()
    val showPhoneDialog by viewModel.showPhoneInputDialog.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // 1. Header Title & Subtitle (Exact match to screenshot)
            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSignUp) "Sign up to get started" else "Sign in to your account",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 2. Sign Up Name Field (Visible when in Sign Up mode)
            if (isSignUp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Full Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.inputName.value = it },
                        placeholder = {
                            Text(text = "John Doe", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name Icon",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        textStyle = TextStyle(fontSize = 15.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        )
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // 3. Email Address Field (Exact match to screenshot)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.inputEmail.value = it },
                    placeholder = {
                        Text(text = "your@email.com", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email Icon",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    textStyle = TextStyle(fontSize = 15.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Password Field (Exact match to screenshot)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.inputPassword.value = it },
                    placeholder = {
                        Text(text = "••••••••", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Icon",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 15.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    )
                )
            }

            // 5. Forgot Password Link (Right aligned, visible in Sign In mode)
            if (!isSignUp) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable {
                            viewModel.authErrorMessage.value = "পাসওয়ার্ড রিসেটের লিংক আপনার ইমেইলে পাঠানো হবে"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!authError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = authError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 6. Primary Action Button: "Sign In" / "Sign Up" (Exact match to screenshot)
            Button(
                onClick = { viewModel.performLoginOrSignUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = if (isSignUp) "Sign Up" else "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 7. Divider: "Or continue with" (Exact match to screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFE5E7EB)
                )
                Text(
                    text = "Or continue with",
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFE5E7EB)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 8. Google Sign-In Button (Exact match to screenshot)
            OutlinedButton(
                onClick = { viewModel.performGoogleSignIn(activity = context as? Activity) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleGLogo(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 9. Bottom Sign In / Sign Up Link (Exact match to screenshot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
                Text(
                    text = if (isSignUp) "Sign in" else "Sign up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.clickable { viewModel.toggleAuthMode() }
                )
            }
        }

        // 10. Phone Number Input Modal (When phone is required for direct login without OTP)
        if (showPhoneDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showPhoneInputDialog.value = false },
                title = {
                    Text(
                        text = "আপনার মোবাইল নম্বর দিন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "লগইন সম্পূর্ণ করতে আপনার ১১ ডিজিটের মোবাইল নম্বরটি লিখুন। কোনো ওটিপি কোড লাগবে না।",
                            fontSize = 13.5.sp,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.inputPhone.value = it },
                            placeholder = { Text("01XXXXXXXXX") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.submitPhoneForDirectLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("লগইন করুন →", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showPhoneInputDialog.value = false }) {
                        Text("বাতিল", color = Color(0xFF6B7280))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun OtpVerificationScreen(viewModel: EBloodViewModel) {
    val otpCode by viewModel.otpCode.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val countdown by viewModel.otpCountdown.collectAsStateWithLifecycle()
    val phone by viewModel.inputPhone.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val displayPhone = if (phone.isNotBlank()) phone else "1969114300"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp)
            .testTag("otp_verify_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.AUTH) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = TextSecondary, fontSize = 14.sp)
                    }
                }

                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Lock Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFEF3C7), CircleShape)
                    .border(2.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "OTP Lock",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "ওটিপি যাচাই করুন",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$displayPhone নম্বরে এসএমএস (SMS)-এর মাধ্যমে পাঠানো ৬ ডিজিটের ওটিপি কোডটি লিখুন",
                fontSize = 13.5.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!authError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrimsonPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, CrimsonPrimary, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ $authError",
                        color = CrimsonPrimary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            val focusRequester = remember { FocusRequester() }
            val context = LocalContext.current
            val isSendingSms by viewModel.isSendingSms.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            // WhatsApp Style Auto-Detect Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSendingSms || isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF16A34A),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isVerifying) "ওটিপি স্বয়ংক্রিয়ভাবে যাচাই করা হচ্ছে..." else "এসএমএস আসলে কোডটি নিজে থেকেই বসে যাবে...",
                        fontSize = 12.sp,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // 6 OTP Digit Boxes with Direct Keyboard Input Support
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { focusRequester.requestFocus() },
                contentAlignment = Alignment.Center
            ) {
                // Overlay text field so typing on keyboard fills the 6 boxes
                BasicTextField(
                    value = otpCode,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            viewModel.otpCode.value = input
                            if (input.length == 6) {
                                viewModel.verifyOtp()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .matchParentSize()
                        .alpha(0.01f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0 until 6) {
                        val char = if (i < otpCode.length) otpCode[i].toString() else ""
                        val isCurrent = i == otpCode.length
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) CrimsonPrimary else if (char.isNotEmpty()) CrimsonPrimary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (char.isNotEmpty()) char else if (isCurrent) "|" else "·",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (char.isNotEmpty()) TextPrimary else if (isCurrent) CrimsonPrimary else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "বক্সে ট্যাপ করে আপনার এসএমএস ওটিপি কোড লিখুন",
                fontSize = 11.5.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verify & Continue -> Button
            Button(
                onClick = { viewModel.verifyOtp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_otp_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = otpCode.length == 6 && !isVerifying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    disabledContainerColor = CrimsonPrimary.copy(alpha = 0.4f)
                )
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "কোড যাচাই করুন →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (countdown > 0) "পুনরায় কোড পাঠানোর সময়: ${countdown} সেকেন্ড" else "কোড পাননি? পুনরায় ওটিপি কোড পাঠান",
                fontSize = 13.sp,
                color = if (countdown > 0) TextMuted else CrimsonPrimary,
                modifier = Modifier.clickable(enabled = countdown == 0) {
                    val act = context as? Activity
                    viewModel.onSendOtpClicked(activity = act)
                    viewModel.proceedToEnterOtp()
                }
            )
        }
    }
}

@Composable
fun NameInputScreen(viewModel: EBloodViewModel) {
    val name by viewModel.inputName.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val appLogoUrl by viewModel.appLogoUrl.collectAsStateWithLifecycle()
    val backendUrl by viewModel.backendServerUrl.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("name_input_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.OTP_VERIFY) },
                    modifier = Modifier.testTag("back_to_otp_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFFFCDD2), CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(
                    logoUrl = appLogoUrl,
                    backendBaseUrl = backendUrl,
                    contentDescription = "EBlood Logo",
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "EBloodDonation",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonPrimary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Card Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x1A000000),
                        spotColor = Color(0x1A000000)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "আপনার নাম লিখুন",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "রক্তদান বা জরুরি প্রয়োজনে যোগাযোগের জন্য আপনার নাম লিখুন।",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!authError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = authError ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = "আপনার নাম *",
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            viewModel.inputName.value = it
                            if (!authError.isNullOrBlank()) {
                                viewModel.authErrorMessage.value = null
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Enter your name",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_after_otp_field"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = CrimsonPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.submitName(name = name, goToProfileSetup = true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_name_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary
                        )
                    ) {
                        Text(
                            text = "পরবর্তী ধাপ (রক্তের গ্রুপ নির্বাচন) →",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "সরাসরি অ্যাপে প্রবেশ করুন",
                            fontSize = 13.5.sp,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.submitName(name = name, goToProfileSetup = false)
                                }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "আপনার নাম ও রক্তের তথ্য অ্যাপে নিরাপদে সংরক্ষিত থাকবে",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: EBloodViewModel) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val selectedBloodGroup by viewModel.setupBloodGroup.collectAsStateWithLifecycle()
    val location by viewModel.setupLocation.collectAsStateWithLifecycle()
    val address by viewModel.setupAddress.collectAsStateWithLifecycle()
    val name by viewModel.inputName.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()
    val locationFeedback by viewModel.locationStatusFeedback.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.detectAndSetLocation(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp)
            .testTag("profile_setup_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Complete Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select your blood group and location so nearby patients can find you in an emergency.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner (e.g. if name is already taken)
            if (!authError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = authError ?: "",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = "আপনার নাম *",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    viewModel.inputName.value = it
                    if (!authError.isNullOrBlank()) viewModel.authErrorMessage.value = null
                },
                placeholder = { Text("Enter your name", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CrimsonPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_name_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Blood Group *",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4x2 grid of blood groups
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bloodGroups.take(4).forEach { bg ->
                        val isSelected = selectedBloodGroup == bg
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CrimsonPrimary else DarkSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CrimsonPrimary else DarkSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setupBloodGroup.value = bg }
                                .testTag("setup_blood_group_$bg"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bloodGroups.drop(4).forEach { bg ->
                        val isSelected = selectedBloodGroup == bg
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CrimsonPrimary else DarkSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CrimsonPrimary else DarkSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setupBloodGroup.value = bg }
                                .testTag("setup_blood_group_$bg"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Area / City Header with Location Check & Set Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Area / City *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Option to check & set location
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isDetectingLocation) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        .testTag("check_and_set_location_button"),
                    color = Color(0xFF064E3B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFA7F3D0)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "চেক হচ্ছে...",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "📍", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "লোকেশন চেক ও সেট",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.setupLocation.value = it },
                placeholder = { Text("এলাকা বা শহরের নাম লিখুন (e.g. Uttara, Dhaka)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonPrimary) },
                modifier = Modifier.fillMaxWidth().testTag("setup_location_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Location Feedback (if auto-detected or updated)
            if (!locationFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = locationFeedback ?: "",
                    fontSize = 11.5.sp,
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick area chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ঢাকা", "উত্তরা", "ধানমন্ডি", "মিরপুর", "চট্টগ্রাম", "সিলেট").forEach { area ->
                    val isCurrentArea = location == area
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrentArea) CrimsonPrimary else DarkSurfaceElevated)
                            .clickable {
                                viewModel.setupLocation.value = area
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = area,
                            fontSize = 11.sp,
                            color = if (isCurrentArea) Color.White else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Full Street Address *",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { viewModel.setupAddress.value = it },
                placeholder = { Text("রাস্তা ও বাসার ঠিকানা (e.g. Sector 11, Road 4, House 12)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().testTag("setup_address_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.completeProfileSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("complete_profile_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text(
                    text = "Save Profile & Enter App →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
