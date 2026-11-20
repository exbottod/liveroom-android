package com.venderlive.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.ui.login.AllCountries

internal val FlagTeal = Color(0xFF1CDBBA)
internal val InkText = Color(0xFF1C1F2A)
internal val MutedText = Color(0xFF8B90A0)

/** Flag emoji for a two-letter country code, e.g. "ID" -> 🇮🇩. */
internal fun flagOf(country: String?): String {
    if (country.isNullOrBlank()) return ""
    return AllCountries.firstOrNull { it.iso.equals(country, ignoreCase = true) }?.flag ?: ""
}

internal fun countryNameOf(country: String?): String? {
    if (country.isNullOrBlank()) return null
    return AllCountries.firstOrNull { it.iso.equals(country, ignoreCase = true) }?.name
}

/** Every country, searchable: the location picker behind the flag. */
@Composable
internal fun CountryPickerDialog(
    selected: String?,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        if (query.isBlank()) AllCountries
        else AllCountries.filter { it.name.contains(query, true) || it.dial.contains(query) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih negara") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Cari negara") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(results.size) { i ->
                        val c = results[i]
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPick(c.iso) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(c.flag, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(c.name, color = InkText, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            if (c.iso == selected) Text("✓", color = FlagTeal, fontSize = 15.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
    )
}
