package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

/**
 * Universal dynamic logo component that respects dynamic logo uploads from Admin Panel
 * with seamless fallback to high-resolution local vector brand logo.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    backendBaseUrl: String? = null,
    contentDescription: String = "EBlood Logo"
) {
    val context = LocalContext.current

    val resolvedUrl = remember(logoUrl, backendBaseUrl) {
        val trimmed = logoUrl?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed == "/logo.svg") {
            ""
        } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:")) {
            trimmed
        } else if (trimmed.startsWith("/")) {
            val base = (backendBaseUrl ?: "").trim().trimEnd('/')
            if (base.isNotBlank()) "$base$trimmed" else ""
        } else {
            trimmed
        }
    }

    if (resolvedUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resolvedUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.ic_eblood_logo),
            placeholder = painterResource(id = R.drawable.ic_eblood_logo)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_eblood_logo),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
