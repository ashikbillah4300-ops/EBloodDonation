package com.example.util

/**
 * Utility for handling Bangladesh mobile numbers with fixed "8801" prefix (13 digits total).
 * Full format: 8801XXXXXXXXX (13 digits)
 * E.164 format: +8801XXXXXXXXX (14 characters)
 * Domestic format: 01XXXXXXXXX (11 digits)
 */
object BangladeshPhoneUtils {
    const val PREFIX = "8801"
    const val TOTAL_DIGITS = 13 // 8801 + 9 digits = 13 digits

    /**
     * Cleans any pasted or typed text into the 9 digits following "8801".
     * Handles:
     * - "+8801712345678" -> "712345678"
     * - "8801712345678"  -> "712345678"
     * - "01712345678"    -> "712345678"
     * - "018-1234-5678"  -> "812345678"
     * - "712345678"      -> "712345678"
     */
    fun extractSuffixDigits(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        val suffix = when {
            digits.startsWith("8801") -> digits.substring(4)
            digits.startsWith("01") -> digits.substring(2)
            digits.startsWith("880") -> digits.substring(3)
            digits.startsWith("1") && digits.length > 1 && digits[1] in '3'..'9' -> digits.substring(1)
            else -> digits
        }
        return suffix.take(9)
    }

    /**
     * Formats into full 13-digit number (8801XXXXXXXXX)
     */
    fun toFull13Digits(raw: String): String {
        val suffix = extractSuffixDigits(raw)
        return "$PREFIX$suffix"
    }

    /**
     * Formats into E.164 international format (+8801XXXXXXXXX) for Firebase Phone Auth
     */
    fun toE164Format(raw: String): String {
        return "+${toFull13Digits(raw)}"
    }

    /**
     * Formats into domestic 11-digit format (01XXXXXXXXX) for Room database and display
     */
    fun toDomesticFormat(raw: String): String {
        val suffix = extractSuffixDigits(raw)
        return "01$suffix"
    }

    /**
     * Validates whether the 13-digit number has all 13 digits and a valid Bangladesh operator:
     * BD mobile operators: 013 (GP), 014 (BL), 015 (Teletalk), 016 (Airtel), 017 (GP), 018 (Robi), 019 (BL)
     * Corresponding to 5th digit (index 4) being 3, 4, 5, 6, 7, 8, or 9.
     */
    fun isValidBdNumber(raw: String): Boolean {
        val full = toFull13Digits(raw)
        if (full.length != TOTAL_DIGITS) return false
        val operatorDigit = full[4]
        return operatorDigit in '3'..'9'
    }
}
