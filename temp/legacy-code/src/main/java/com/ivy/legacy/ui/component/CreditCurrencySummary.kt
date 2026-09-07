package com.ivy.legacy.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.legacy.data.model.CreditCurrencyStats
import com.ivy.legacy.utils.format
import com.ivy.ui.R

/** One compact column per currency; never combines amounts from different currencies. */
@Composable
fun CreditCurrencySummary(stats: List<CreditCurrencyStats>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { item ->
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.currency, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.amount_to_pay), style = MaterialTheme.typography.labelSmall)
                            Text(item.toPay.format(item.currency), style = MaterialTheme.typography.titleMedium,
                                color = if (item.toPay > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            HorizontalDivider(Modifier.padding(vertical = 2.dp))
                            Text(stringResource(R.string.limit_left), style = MaterialTheme.typography.labelSmall)
                            Text(
                                item.available?.let { (if (item.estimated) "≈ " else "") + it.format(item.currency) }
                                    ?: stringResource(R.string.credit_unavailable),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(if (item.limitIsCap) R.string.credit_limit_or_cap else R.string.limit_label) + " " + item.limit.format(item.currency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
