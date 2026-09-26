package com.ivy.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.legacy.data.model.*
import com.ivy.legacy.domain.validMoney
import com.ivy.legacy.ui.component.CreditCurrencySummary
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreditCardEditor(
    card: CreditCardData?,
    baseCurrency: String,
    busy: Boolean,
    error: String?,
    onSave: (CreditCardInput) -> Unit,
    onDismiss: () -> Unit,
) {
    val primary = card?.primary?.account
    val secondary = card?.secondary?.account
    var name by rememberSaveable { mutableStateOf(primary?.name?.value.orEmpty()) }
    var currency by rememberSaveable { mutableStateOf(primary?.asset?.code ?: baseCurrency) }
    var limit by rememberSaveable { mutableStateOf(primary?.creditLimit?.plain().orEmpty()) }
    var dual by rememberSaveable { mutableStateOf(secondary != null) }
    var secondCurrency by rememberSaveable { mutableStateOf(secondary?.asset?.code ?: if (currency == "USD") "BDT" else "USD") }
    var secondLimit by rememberSaveable { mutableStateOf(secondary?.creditLimit?.plain().orEmpty()) }
    var shared by rememberSaveable { mutableStateOf(if (secondary != null) card.shared else true) }
    var rate by rememberSaveable { mutableStateOf(primary?.creditExchangeRate?.plain().orEmpty()) }
    var color by rememberSaveable { mutableStateOf(primary?.color?.value ?: Color(0xFF6750A4).toArgb()) }
    var icon by rememberSaveable { mutableStateOf(primary?.icon?.id ?: "ic_vue_money_card") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val limitValue = limit.number()
    val secondValue = secondLimit.number()
    val rateValue = rate.number()
    val valid = name.isNotBlank() && limitValue?.let { validMoney(it, currency) } == true &&
        (!dual || (currency != secondCurrency && secondValue?.let { validMoney(it, secondCurrency) } == true &&
            (!shared || rateValue?.let { it.isFinite() && it > 0 } == true)))
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
            .padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(if (card == null) R.string.new_credit_card else R.string.edit_credit_card),
                style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.card_name)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !busy)
            CurrencyChoice(stringResource(R.string.main_currency), currency, enabled = primary == null && !busy) { currency = it }
            MoneyField(limit, { limit = it }, stringResource(R.string.credit_limit_in, currency), !busy)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dual_currency_card), Modifier.weight(1f))
                Switch(checked = dual, onCheckedChange = { dual = it }, enabled = secondary == null && !busy)
            }
            if (primary != null) HelperText(stringResource(R.string.credit_currency_locked))
            if (dual) {
                CurrencyChoice(stringResource(R.string.second_currency), secondCurrency, enabled = secondary == null && !busy) { secondCurrency = it }
                MoneyField(secondLimit, { secondLimit = it },
                    stringResource(if (shared) R.string.credit_cap_in else R.string.credit_limit_in, secondCurrency), !busy)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.shared_credit_limit), Modifier.weight(1f))
                    Switch(checked = shared, onCheckedChange = { shared = it }, enabled = !busy)
                }
                HelperText(stringResource(if (shared) R.string.shared_credit_help else R.string.separate_credit_help))
                if (shared) {
                    MoneyField(rate, { rate = it }, stringResource(R.string.credit_rate_label, secondCurrency, currency), !busy)
                    HelperText(stringResource(R.string.credit_rate_help))
                }
            }
            Text(stringResource(R.string.credit_color), style = MaterialTheme.typography.labelLarge)
            val colorLabel = stringResource(R.string.credit_color)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(color, 0xFF6750A4.toInt(), 0xFF006A6A.toInt(), 0xFF215FA6.toInt(), 0xFF936900.toInt(), 0xFFBA1A1A.toInt())
                    .distinct().forEach { candidate ->
                        FilledIconToggleButton(checked = color == candidate, onCheckedChange = { color = candidate }, enabled = !busy,
                            modifier = Modifier.semantics { contentDescription = "$colorLabel #${Integer.toHexString(candidate).takeLast(6)}" },
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color(candidate), checkedContainerColor = Color(candidate))) {
                            Text(if (color == candidate) "✓" else "", color = Color.White)
                        }
                    }
            }
            Text(stringResource(R.string.credit_icon), style = MaterialTheme.typography.labelLarge)
            val iconLabel = stringResource(R.string.credit_icon)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(icon, "ic_vue_money_card", "ic_custom_bank_s", "ic_custom_account_s").distinct().forEachIndexed { index, candidate ->
                    FilledIconToggleButton(checked = icon == candidate, onCheckedChange = { icon = candidate }, enabled = !busy,
                        modifier = Modifier.semantics { contentDescription = "$iconLabel ${index + 1}" }) {
                        ItemIconSDefaultIcon(iconName = candidate, defaultIcon = R.drawable.ic_custom_account_s,
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (attempted && !valid) ErrorText(stringResource(R.string.credit_form_invalid))
            error?.let { ErrorText(it) }
            Button(onClick = {
                attempted = true
                if (valid) onSave(CreditCardInput(primary?.id, name.trim(), currency, limitValue!!, color, icon,
                    secondCurrency.takeIf { dual }, secondValue.takeIf { dual }, dual && shared, rateValue.takeIf { dual && shared }))
            }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (busy) R.string.credit_saving else R.string.save))
            }
            TextButton(onClick = onDismiss, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreditCardDetailsSheet(
    card: CreditCardData,
    payableAccounts: List<AccountData>,
    busy: Boolean,
    error: String?,
    onPay: (CreditCardPaymentInput) -> Unit,
    onReset: (AccountData, Double) -> Unit,
    onEdit: () -> Unit,
    onViewTransactions: (AccountData) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedCurrency by rememberSaveable { mutableStateOf(card.primary.account.asset.code) }
    val selected = card.accounts.firstOrNull { it.account.asset.code == selectedCurrency } ?: card.primary
    val owed = (-selected.balance).coerceAtLeast(0.0)
    var sourceId by rememberSaveable { mutableStateOf<String?>(null) }
    val source = payableAccounts.firstOrNull { it.account.id.value.toString() == sourceId }
    var chooseSource by remember { mutableStateOf(false) }
    var paid by rememberSaveable(selected.account.id.value.toString()) { mutableStateOf(owed.plain()) }
    var debit by rememberSaveable(sourceId, selectedCurrency) { mutableStateOf("") }
    var attempted by rememberSaveable(selectedCurrency) { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val crossCurrency = source != null && source.account.asset != selected.account.asset
    val paidValue = paid.number()
    val debitValue = if (crossCurrency) debit.number() else paidValue
    val valid = source != null && paidValue?.let { validMoney(it, selected.account.asset.code) && it <= owed } == true &&
        debitValue?.let { validMoney(it, source.account.asset.code) } == true
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
            .padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(card.primary.account.name.value, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onEdit, enabled = !busy) { Text(stringResource(R.string.edit)) }
            }
            CreditCurrencySummary(card.stats())
            if (card.shared) HelperText(stringResource(R.string.credit_shared_note))
            if (card.secondary != null) {
                Text(stringResource(R.string.credit_payment_currency), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    card.accounts.forEach { account ->
                        FilterChip(selected = selectedCurrency == account.account.asset.code,
                            onClick = { selectedCurrency = account.account.asset.code }, enabled = !busy,
                            label = { Text(account.account.asset.code) })
                    }
                }
            }
            if (owed > 0) {
                if (payableAccounts.isEmpty()) HelperText(stringResource(R.string.credit_no_payable_accounts))
                else {
                    OutlinedButton(onClick = { chooseSource = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Text(source?.let { "${it.account.name.value} · ${it.account.asset.code}" } ?: stringResource(R.string.credit_pay_from))
                    }
                    MoneyField(paid, { paid = it }, stringResource(R.string.credit_paid_amount, selected.account.asset.code), !busy)
                    if (crossCurrency) {
                        MoneyField(debit, { debit = it }, stringResource(R.string.credit_debited_amount, source!!.account.asset.code), !busy)
                        HelperText(stringResource(R.string.credit_payment_help))
                    }
                    if (attempted && !valid) ErrorText(stringResource(R.string.credit_payment_invalid))
                    error?.let { ErrorText(it) }
                    Button(onClick = {
                        attempted = true
                        if (valid) onPay(CreditCardPaymentInput(selected.account.id, source!!.account.id, paidValue!!, debitValue!!))
                    }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (busy) R.string.credit_saving else R.string.credit_payment_record))
                    }
                    HelperText(stringResource(R.string.credit_payment_disclaimer))
                }
            } else HelperText(stringResource(R.string.credit_all_paid))
            TextButton(onClick = { onViewTransactions(selected) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.credit_view_currency_transactions, selected.account.asset.code))
            }
            if (owed > 0) TextButton(onClick = { confirmReset = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.reset_card))
            }
            TextButton(onClick = onDismiss, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
        }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text(stringResource(R.string.credit_reset_title)) },
        text = { Text(stringResource(R.string.credit_reset_help, owed.format(selected.account.asset.code), selected.account.asset.code)) },
        confirmButton = { TextButton(onClick = { confirmReset = false; onReset(selected, owed) }) { Text(stringResource(R.string.reset_card)) } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.close)) } },
    )
    if (chooseSource) SearchChoiceDialog(
        title = stringResource(R.string.credit_pay_from),
        searchLabel = stringResource(R.string.search_accounts),
        choices = payableAccounts.map { it.account.id.value.toString() to "${it.account.name.value} · ${it.account.asset.code}" },
        selected = sourceId,
        onSelect = { sourceId = it; chooseSource = false },
        onDismiss = { chooseSource = false },
    )
}

@Composable
private fun CurrencyChoice(label: String, value: String, enabled: Boolean, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text("$label · $value")
    }
    if (expanded) {
        val choices = remember {
            Currency.getAvailableCurrencies().sortedBy { it.currencyCode }.map { it.currencyCode to "${it.currencyCode} · ${it.displayName}" }
        }
        SearchChoiceDialog(stringResource(R.string.credit_choose_currency), stringResource(R.string.credit_search_currency),
            choices, value, { onChange(it); expanded = false }, { expanded = false })
    }
}

@Composable
private fun SearchChoiceDialog(
    title: String, searchLabel: String, choices: List<Pair<String, String>>, selected: String?,
    onSelect: (String) -> Unit, onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = choices.filter { it.second.contains(query.trim(), ignoreCase = true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text(searchLabel) }, singleLine = true)
            if (matches.isEmpty()) Text(stringResource(R.string.no_matching_accounts))
            LazyColumn(Modifier.heightIn(max = 320.dp)) {
                items(matches, key = { it.first }) { choice ->
                    Row(Modifier.fillMaxWidth().clickable { onSelect(choice.first) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == choice.first, onClick = null)
                        Text(choice.second, Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } })
}

@Composable
private fun MoneyField(value: String, onChange: (String) -> Unit, label: String, enabled: Boolean) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun HelperText(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ErrorText(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

private fun Double.plain(): String = toBigDecimal().stripTrailingZeros().toPlainString()
private fun String.number(): Double? = com.ivy.legacy.domain.parseCreditAmount(this)
