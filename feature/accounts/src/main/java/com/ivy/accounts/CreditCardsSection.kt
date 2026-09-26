package com.ivy.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.legacy.data.model.CreditCardData
import com.ivy.legacy.ui.component.CreditCurrencySummary
import com.ivy.ui.R
import androidx.compose.ui.graphics.Color
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon

@Composable
fun CreditCardsSection(
    cards: List<CreditCardData>,
    onCardClick: (CreditCardData) -> Unit,
    onAddCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.credit_cards), Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onAddCard) { Text(stringResource(R.string.add)) }
        }
        cards.forEach { card ->
            ElevatedCard(onClick = { onCardClick(card) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ItemIconSDefaultIcon(iconName = card.primary.account.icon?.id,
                            defaultIcon = R.drawable.ic_custom_account_s,
                            tint = Color(card.primary.account.color.value))
                        Text(card.primary.account.name.value, style = MaterialTheme.typography.titleMedium)
                    }
                    CreditCurrencySummary(card.stats())
                    if (card.secondary != null) Text(
                        stringResource(if (card.shared) R.string.credit_shared_note else R.string.credit_separate_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
