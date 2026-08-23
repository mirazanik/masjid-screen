package com.mirazanik.masjidscreen.admin.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val policyUrl = stringResource(R.string.privacy_policy_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl)))
                        }
                    ) {
                        Text("Open in browser")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrivacyPolicyBody()
        }
    }
}

@Composable
fun PrivacyPolicyLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Privacy Policy",
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun PrivacyPolicyBody() {
    PolicySection("Last updated", "23 August 2026")
    PolicySection(
        "Who we are",
        "MasjidScreen is a mosque display app. It shows prayer times, jamaat times, hadiths, and notices on a tablet or TV. Mosque admins sign in to manage that content."
    )
    PolicySection(
        "What we collect",
        "Admin accounts: email address, display name, and a Google account identifier if you sign in with Google.\n\n" +
            "Mosque content you enter: mosque name and address, latitude and longitude (typed in settings — the app does not use GPS), prayer calculation settings, jamaat times, hadiths, and notices.\n\n" +
            "Display devices: a generated device ID, pairing status, app version, last-seen time, and screen size. These keep the TV paired and in sync.\n\n" +
            "Notifications: a Firebase Cloud Messaging token so admins can receive alerts.\n\n" +
            "Camera: used only to scan a pairing QR code. Photos and videos are not saved, uploaded, or shared."
    )
    PolicySection(
        "What we do not collect",
        "We do not collect precise GPS location, contacts, photos, videos, payment information, or advertising IDs. The app does not show ads."
    )
    PolicySection(
        "How we use data",
        "To create and protect admin accounts, sync mosque content to display screens, calculate prayer times from the location you enter, pair devices, and send admin notifications."
    )
    PolicySection(
        "Where data is stored",
        "Account and mosque data are stored in Google Firebase (Authentication, Cloud Firestore, and Cloud Messaging). Google processes this data on our behalf. Data is sent over HTTPS."
    )
    PolicySection(
        "Sharing",
        "We do not sell your data. We do not share it with other companies for advertising. Google receives data only as our infrastructure provider."
    )
    PolicySection(
        "Retention and deletion",
        "Account and mosque data stay until an admin deletes them or asks us to delete them. To request deletion of an account or mosque data, use the contact email on the Google Play listing, or open an issue at github.com/mirazanik/masjid-screen/issues. We will delete the requested data from Firebase within a reasonable time unless we must keep it for security or legal reasons."
    )
    PolicySection(
        "Children",
        "MasjidScreen is not directed at children under 13. We do not knowingly collect personal information from children."
    )
    PolicySection(
        "Changes",
        "If this policy changes, we will update the date above and the copy in the app."
    )
    PolicySection(
        "Contact",
        "Use the support email shown on the Google Play listing for MasjidScreen, or github.com/mirazanik/masjid-screen/issues."
    )
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
