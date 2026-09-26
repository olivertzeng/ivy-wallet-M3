package com.ivy.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.component.CreditCurrencySummary
import com.ivy.ui.R

@Composable
fun CreditCardsSummaryCard(
    summary: CreditCardsSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(onClick = onClick, modifier = modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.credit_cards), style = MaterialTheme.typography.titleMedium)
            CreditCurrencySummary(summary.currencies)
            if (summary.currencies.any { it.estimated }) Text(
                stringResource(R.string.credit_shared_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
