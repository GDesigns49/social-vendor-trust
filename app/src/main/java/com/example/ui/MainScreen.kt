package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TrustCheckerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
    ) {
        // 1. Sleek Header Bar matching design HTML
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .border(width = 1.dp, color = SleekBorder)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 18.sp, color = Color.White)
                }
                Column {
                    Text(
                        text = "Trust Checker",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SleekText,
                            lineHeight = 16.sp
                        )
                    )
                    Text(
                        text = "Vendor Analysis Dashboard",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SleekTextVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 18.sp)
            }
        }

        // Main content area
        Box(
            modifier = Modifier
                .fillModelWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Conditional Rendering based on state
                when {
                    state.isAnalyzing -> {
                        // A. LOADING STATE
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SleekBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    color = SleekPrimary,
                                    strokeWidth = 5.dp
                                )
                                Text(
                                    text = "Analyzing vendor...",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekText
                                )
                                Text(
                                    text = state.analyzingStage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    state.showResults -> {
                        // B. RESULTS SCREEN
                        
                        // Main Score Presentation Card (SleekResultCard background #d7e8cd)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekResultCard),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Circular Progress Score Gauge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(132.dp)
                                        .padding(4.dp)
                                ) {
                                    val scoreSweep = (state.trustScore / 100f) * 360f
                                    val animatedScoreSweep by animateFloatAsState(
                                        targetValue = scoreSweep,
                                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                    )
                                    val animatedScoreText by animateIntAsState(
                                        targetValue = state.trustScore,
                                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                    )

                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Track circle
                                        drawCircle(
                                            color = Color(0xFFB7CCB0),
                                            style = Stroke(width = 12.dp.toPx())
                                        )
                                        // Active arc
                                        drawArc(
                                            color = SleekPrimary,
                                            startAngle = -90f,
                                            sweepAngle = animatedScoreSweep,
                                            useCenter = false,
                                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$animatedScoreText",
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 36.sp
                                            ),
                                            color = SleekText,
                                            modifier = Modifier.testTag("trust_score_text")
                                        )
                                        Text(
                                            text = "SCORE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = SleekTextVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Risk Level Badge & Description
                                val riskBadgeColor = when (state.riskLevel) {
                                    "High Risk" -> Color(0xFFEF4444)
                                    "Medium Risk" -> Color(0xFFF59E0B)
                                    else -> SleekPrimary
                                }

                                Surface(
                                    color = riskBadgeColor,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .testTag("risk_level_badge")
                                        .padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = state.riskLevel,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                    )
                                }

                                val riskDescription = when (state.riskLevel) {
                                    "High Risk" -> "This vendor demonstrates extreme risk indicators. Transacting is highly discouraged."
                                    "Medium Risk" -> "Exercise reasonable caution. Some borderline indicators detected."
                                    else -> "This vendor demonstrates strong positive behavioral indicators."
                                }

                                Text(
                                    text = riskDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }

                        // Vendor Identity Info Block
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SleekBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFF0F4EB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👤", fontSize = 18.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "VENDOR IDENTITY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = SleekTextVariant
                                        )
                                        Text(
                                            text = "@${state.vendorName}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SleekText
                                        )
                                    }
                                }

                                // Platform Badge
                                Surface(
                                    color = Color(0xFFE7E0EB),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text(
                                        text = state.platform.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Grid / Key Trust Attributes Breakdown matching design HTML
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val ageScoreText = when (state.accountAge) {
                                AccountAge.UNDER_3_MONTHS -> "-20"
                                AccountAge.THREE_TO_TWELVE_MONTHS -> "+5"
                                AccountAge.OVER_1_YEAR -> "+20"
                            }
                            val followersCount = state.followers.toIntOrNull() ?: 0
                            val followersScoreText = when {
                                followersCount < 500 -> "-10"
                                followersCount in 500..5000 -> "+5"
                                else -> "+10"
                            }
                            val engagementScoreText = when (state.engagement) {
                                EngagementLevel.LOW -> "-15"
                                EngagementLevel.MEDIUM -> "+5"
                                EngagementLevel.HIGH -> "+15"
                            }
                            val redFlagsScoreText = if (state.redFlags.isEmpty()) "0" else "-${state.redFlags.size * 10}"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AttributeCard(
                                    title = "Account Age",
                                    value = state.accountAge.displayName,
                                    pointsText = ageScoreText,
                                    modifier = Modifier.weight(1f)
                                )
                                AttributeCard(
                                    title = "Followers",
                                    value = if (state.followers.isEmpty()) "0" else state.followers,
                                    pointsText = followersScoreText,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AttributeCard(
                                    title = "Engagement",
                                    value = state.engagement.displayName,
                                    pointsText = engagementScoreText,
                                    modifier = Modifier.weight(1f)
                                )
                                AttributeCard(
                                    title = "Red Flags",
                                    value = if (state.redFlags.isEmpty()) "None" else "${state.redFlags.size} Active",
                                    pointsText = redFlagsScoreText,
                                    modifier = Modifier.weight(1f),
                                    isWarning = state.redFlags.isNotEmpty()
                                )
                            }
                        }

                        // Warning/Disclaimer Alert Box matching design CSS precisely
                        Surface(
                            color = SleekAlertBg,
                            border = BorderStroke(1.dp, SleekAlertBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("⚠️", fontSize = 18.sp)
                                Text(
                                    text = "This is an estimated analysis based on user-provided data. Always exercise caution when making financial transactions online.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    ),
                                    color = SleekAlertText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reset button
                        Button(
                            onClick = { viewModel.reset() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("reset_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Check Another Vendor",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    else -> {
                        // C. INPUT FORM SCREEN (showResults = false)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SleekBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Vendor Evaluation Details",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekPrimary
                                )

                                // Username Input
                                OutlinedTextField(
                                    value = state.vendorName,
                                    onValueChange = { viewModel.onVendorNameChange(it) },
                                    label = { Text("Enter Vendor Username") },
                                    placeholder = { Text("e.g. boutique_kicks") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = SleekTextVariant
                                        )
                                    },
                                    isError = !state.isValidUser,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("vendor_name_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Live Validation Error Message
                                if (state.errorMessage.isNotEmpty()) {
                                    Text(
                                        text = state.errorMessage,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .testTag("error_message_text")
                                    )
                                }

                                // Platform Dropdown
                                var platformExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = platformExpanded,
                                    onExpandedChange = { platformExpanded = !platformExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.platform.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Platform") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("platform_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = platformExpanded,
                                        onDismissRequest = { platformExpanded = false }
                                    ) {
                                        Platform.entries.forEach { platform ->
                                            DropdownMenuItem(
                                                text = { Text(platform.displayName) },
                                                onClick = {
                                                    viewModel.onPlatformChange(platform)
                                                    platformExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Account Age Dropdown
                                var ageExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = ageExpanded,
                                    onExpandedChange = { ageExpanded = !ageExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.accountAge.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Account Age") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("account_age_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = ageExpanded,
                                        onDismissRequest = { ageExpanded = false }
                                    ) {
                                        AccountAge.entries.forEach { age ->
                                            DropdownMenuItem(
                                                text = { Text(age.displayName) },
                                                onClick = {
                                                    viewModel.onAccountAgeChange(age)
                                                    ageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Followers Input
                                OutlinedTextField(
                                    value = state.followers,
                                    onValueChange = { viewModel.onFollowersChange(it) },
                                    label = { Text("Followers") },
                                    placeholder = { Text("e.g. 1500") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = SleekTextVariant
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("followers_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Engagement Level Dropdown
                                var engagementExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = engagementExpanded,
                                    onExpandedChange = { engagementExpanded = !engagementExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = state.engagement.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Engagement Level") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engagementExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("engagement_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = engagementExpanded,
                                        onDismissRequest = { engagementExpanded = false }
                                    ) {
                                        EngagementLevel.entries.forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level.displayName) },
                                                onClick = {
                                                    viewModel.onEngagementChange(level)
                                                    engagementExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Red Flags Section
                                Text(
                                    text = "Select Red Flags",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SleekPrimary
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF0F4EB))
                                        .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    RedFlag.entries.forEach { flag ->
                                        val isChecked = state.redFlags.contains(flag)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleRedFlag(flag) }
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                                .testTag(flag.testTag),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { viewModel.toggleRedFlag(flag) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFFEF4444),
                                                    uncheckedColor = SleekTextVariant
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = flag.label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isChecked) Color(0xFFEF4444) else SleekTextVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Submit action button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.checkVendor()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("check_vendor_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(
                                        text = "Check Vendor",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
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

// Sub-component card for specific trust indicators
@Composable
fun AttributeCard(
    title: String,
    value: String,
    pointsText: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = SleekTextVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekText,
                    maxLines = 1
                )
                
                val pointsColor = if (pointsText.startsWith("-")) Color(0xFFEF4444) else if (pointsText == "0") SleekTextVariant else SleekPrimary
                Text(
                    text = pointsText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = pointsColor
                )
            }
        }
    }
}

// Utility extension function to restrict maximum content width on expanded screen sizes (e.g. tablet compatibility)
fun Modifier.fillModelWidth() = this
    .fillMaxWidth()
    .widthIn(max = 600.dp)
