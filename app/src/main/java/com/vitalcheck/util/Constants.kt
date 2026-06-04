package com.vitalcheck.util

object Constants {
    const val FITBIT_AUTH_URL = "https://www.fitbit.com/oauth2/authorize"
    const val FITBIT_TOKEN_URL = "https://api.fitbit.com/oauth2/token"
    const val FITBIT_BASE_URL = "https://api.fitbit.com/"
    const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/"
    const val ANTHROPIC_VERSION = "2023-06-01"
    const val HAIKU_MODEL = "claude-haiku-4-5-20251001"

    const val NOTIFICATION_CHANNEL_ID = "vitalcheck_coach"

    val BLOCKED_PACKAGES = listOf(
        "com.instagram.android",
        "com.snapchat.android",
        "com.google.android.youtube"
    )

    const val WORK_MORNING_SYNC = "morning_sync"
    const val WORK_INTRADAY_SYNC = "intraday_sync"
    const val WORK_EVENING_EVAL = "evening_eval"
    const val WORK_LOCKOUT_CHECK = "lockout_check"
    const val WORK_CALORIE_REMINDER = "calorie_reminder"

    const val PREFS_NAME = "vitalcheck_secure_prefs"
    const val KEY_ACCESS_TOKEN = "fitbit_access_token"
    const val KEY_REFRESH_TOKEN = "fitbit_refresh_token"
    const val KEY_TOKEN_EXPIRY = "fitbit_token_expiry"
    const val KEY_CODE_VERIFIER = "fitbit_code_verifier"
}
