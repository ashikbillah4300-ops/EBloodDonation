package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.BangladeshPhoneUtils

/**
 * Reusable Compose text field with a permanently fixed "8801" prefix for Bangladesh mobile numbers.
 *
 * Requirements satisfied:
 * - Always starts with "8801" already written.
 * - User CANNOT delete, edit, or move the cursor before the "8801" prefix (handles backspace, cut, paste, selection).
 * - User types only remaining 9 digits (total 13 digits: 8801XXXXXXXXX).
 * - Only digits allowed.
 * - Automatically cleans pasted numbers (+8801..., 01..., 8801...) without double prefixes.
 */
@Composable
fun FixedPrefixPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "আপনার মোবাইল নম্বর *",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CrimsonPrimary,
        unfocusedBorderColor = DarkSurfaceBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )
) {
    val suffix = BangladeshPhoneUtils.extractSuffixDigits(value)
    val fullText = "${BangladeshPhoneUtils.PREFIX}$suffix"

    var tfv by remember(value) {
        val targetCursor = fullText.length
        mutableStateOf(TextFieldValue(text = fullText, selection = TextRange(targetCursor)))
    }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = tfv,
            onValueChange = { newVal ->
                val newRawText = newVal.text
                val extractedSuffix = if (newRawText.startsWith(BangladeshPhoneUtils.PREFIX)) {
                    // Normal typing/editing after prefix
                    newRawText.substring(BangladeshPhoneUtils.PREFIX.length)
                        .filter { it.isDigit() }
                        .take(9)
                } else {
                    // User backspaced into prefix, or selected all and pasted
                    BangladeshPhoneUtils.extractSuffixDigits(newRawText)
                }

                val updatedFullText = "${BangladeshPhoneUtils.PREFIX}$extractedSuffix"
                // Never allow cursor before index 4 ("8801")
                val requestedCursor = newVal.selection.end
                val safeCursor = maxOf(BangladeshPhoneUtils.PREFIX.length, minOf(requestedCursor, updatedFullText.length))

                tfv = TextFieldValue(
                    text = updatedFullText,
                    selection = TextRange(safeCursor)
                )
                onValueChange(updatedFullText)
            },
            enabled = enabled,
            singleLine = true,
            isError = isError,
            placeholder = {
                Text(
                    text = "8801XXXXXXXXX",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone Icon",
                    tint = CrimsonPrimary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            shape = shape,
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFDC2626),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

