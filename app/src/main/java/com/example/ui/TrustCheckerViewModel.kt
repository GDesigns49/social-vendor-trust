package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Platform(val displayName: String) {
    INSTAGRAM("Instagram"),
    TWITTER("Twitter"),
    TIKTOK("TikTok"),
    FACEBOOK("Facebook")
}

enum class AccountAge(val displayName: String) {
    UNDER_3_MONTHS("<3 months"),
    THREE_TO_TWELVE_MONTHS("3–12 months"),
    OVER_1_YEAR(">1 year")
}

enum class EngagementLevel(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class RedFlag(val label: String, val testTag: String) {
    COMMENTS_DISABLED("Comments disabled", "red_flag_comments_disabled"),
    NO_TAGGED_CUSTOMERS("No tagged customers", "red_flag_no_tagged_customers"),
    PRICES_TOO_GOOD("Prices too good to be true", "red_flag_prices_too_good"),
    RECENTLY_CREATED("Recently created account", "red_flag_recently_created"),
    NO_PROFILE_PICTURE("No profile picture", "red_flag_no_profile_picture")
}

data class TrustFactorDetail(
    val description: String,
    val pointsChange: Int,
    val isPositive: Boolean
)

data class TrustCheckerState(
    val vendorName: String = "",
    val platform: Platform = Platform.INSTAGRAM,
    val accountAge: AccountAge = AccountAge.THREE_TO_TWELVE_MONTHS,
    val followers: String = "",
    val engagement: EngagementLevel = EngagementLevel.MEDIUM,
    val redFlags: Set<RedFlag> = emptySet(),
    
    // UI states
    val isAnalyzing: Boolean = false,
    val analyzingStage: String = "",
    val isValidUser: Boolean = true,
    val trustScore: Int = 50,
    val riskLevel: String = "", // "High Risk", "Medium Risk", "Low Risk"
    val showResults: Boolean = false,
    val errorMessage: String = "",
    val scoreFactors: List<TrustFactorDetail> = emptyList()
)

class TrustCheckerViewModel : ViewModel() {
    private val _state = MutableStateFlow(TrustCheckerState())
    val state: StateFlow<TrustCheckerState> = _state.asStateFlow()

    fun onVendorNameChange(name: String) {
        _state.update { it.copy(vendorName = name, errorMessage = "", isValidUser = true) }
    }

    fun onPlatformChange(platform: Platform) {
        _state.update { it.copy(platform = platform) }
    }

    fun onAccountAgeChange(age: AccountAge) {
        _state.update { it.copy(accountAge = age) }
    }

    fun onFollowersChange(followers: String) {
        // Only allow numbers
        if (followers.isEmpty() || followers.all { it.isDigit() }) {
            _state.update { it.copy(followers = followers) }
        }
    }

    fun onEngagementChange(engagement: EngagementLevel) {
        _state.update { it.copy(engagement = engagement) }
    }

    fun toggleRedFlag(redFlag: RedFlag) {
        _state.update { currentState ->
            val updatedFlags = if (currentState.redFlags.contains(redFlag)) {
                currentState.redFlags - redFlag
            } else {
                currentState.redFlags + redFlag
            }
            currentState.copy(redFlags = updatedFlags)
        }
    }

    fun validateInput(): Boolean {
        val name = _state.value.vendorName.trim()
        
        // Rules:
        // - Minimum 4 characters
        // - Must NOT be only numbers
        // - Must contain at least ONE letter
        // - Can contain letters and numbers ONLY (no spaces)
        // - Allow underscores (_) and dots (.)
        // - Reject anything else
        
        val meetsLength = name.length >= 4
        val notOnlyNumbers = !name.all { it.isDigit() }
        val hasLetter = name.any { it.isLetter() }
        val allowedCharacters = name.all { it.isLetterOrDigit() || it == '_' || it == '.' }
        
        val isValid = meetsLength && notOnlyNumbers && hasLetter && allowedCharacters && name.isNotEmpty()
        
        if (!isValid) {
            _state.update {
                it.copy(
                    isValidUser = false,
                    showResults = false,
                    errorMessage = "❌ Invalid username. Use letters or a mix of letters and numbers. Usernames cannot be only numbers."
                )
            }
            return false
        }
        
        _state.update { it.copy(isValidUser = true, errorMessage = "") }
        return true
    }

    fun checkVendor() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true, errorMessage = "", showResults = false) }
            
            // Interactive loading simulation steps
            val stages = listOf(
                "Analyzing account age and metadata...",
                "Calculating follower-to-engagement ratio...",
                "Scanning for behavioral red flags...",
                "Generating deep Trust Report..."
            )
            for (stage in stages) {
                _state.update { it.copy(analyzingStage = stage) }
                delay(400)
            }
            
            analyzeVendor()
        }
    }

    private fun analyzeVendor() {
        val currentState = _state.value
        var score = 50
        val factors = mutableListOf<TrustFactorDetail>()

        // 1. Account Age
        // <3 months -> -20, 3-12 months -> +5, >1 year -> +20
        when (currentState.accountAge) {
            AccountAge.UNDER_3_MONTHS -> {
                score -= 20
                factors.add(TrustFactorDetail("Account created within last 3 months", -20, false))
            }
            AccountAge.THREE_TO_TWELVE_MONTHS -> {
                score += 5
                factors.add(TrustFactorDetail("Account age is 3–12 months", 5, true))
            }
            AccountAge.OVER_1_YEAR -> {
                score += 20
                factors.add(TrustFactorDetail("Established account (over 1 year active)", 20, true))
            }
        }

        // 2. Followers
        // <500 -> -10, 500-5000 -> +5, >5000 -> +10
        val followersCount = currentState.followers.toIntOrNull() ?: 0
        when {
            followersCount < 500 -> {
                score -= 10
                factors.add(TrustFactorDetail("Small audience (under 500 followers)", -10, false))
            }
            followersCount in 500..5000 -> {
                score += 5
                factors.add(TrustFactorDetail("Moderate audience (500–5,000 followers)", 5, true))
            }
            followersCount > 5000 -> {
                score += 10
                factors.add(TrustFactorDetail("Large audience (over 5,000 followers)", 10, true))
            }
        }

        // 3. Engagement
        // Low -> -15, Medium -> +5, High -> +15
        when (currentState.engagement) {
            EngagementLevel.LOW -> {
                score -= 15
                factors.add(TrustFactorDetail("Low user engagement rates detected", -15, false))
            }
            EngagementLevel.MEDIUM -> {
                score += 5
                factors.add(TrustFactorDetail("Healthy medium engagement levels", 5, true))
            }
            EngagementLevel.HIGH -> {
                score += 15
                factors.add(TrustFactorDetail("Exceptional, highly active engagement", 15, true))
            }
        }

        // 4. Red Flags
        // Each selected -> -10
        currentState.redFlags.forEach { flag ->
            score -= 10
            factors.add(TrustFactorDetail("Red Flag: ${flag.label}", -10, false))
        }

        // Clamp score between 0 and 100
        val finalScore = score.coerceIn(0, 100)

        // Risk Level:
        // 0-40 -> "High Risk"
        // 41-70 -> "Medium Risk"
        // 71-100 -> "Low Risk"
        val level = when (finalScore) {
            in 0..40 -> "High Risk"
            in 41..70 -> "Medium Risk"
            else -> "Low Risk"
        }

        _state.update {
            it.copy(
                isAnalyzing = false,
                trustScore = finalScore,
                riskLevel = level,
                scoreFactors = factors,
                showResults = true
            )
        }
    }

    fun reset() {
        _state.value = TrustCheckerState()
    }
}
