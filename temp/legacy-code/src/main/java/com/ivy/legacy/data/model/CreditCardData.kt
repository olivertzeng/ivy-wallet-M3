package com.ivy.legacy.data.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.AccountId
import com.ivy.data.model.isSecondaryCreditCurrency
import com.ivy.wallet.domain.data.IvyCurrency
import java.math.BigDecimal
import java.math.RoundingMode

@Immutable
data class CreditCurrencyStats(
    val currency: String,
    val toPay: Double,
    val limit: Double,
    val available: Double?,
    val estimated: Boolean = false,
    val limitIsCap: Boolean = false,
)

@Immutable
data class CreditCardData(
    val primary: AccountData,
    val secondary: AccountData? = null,
) {
    val accounts: List<AccountData> get() = listOfNotNull(primary, secondary)
    val shared: Boolean get() = secondary != null && primary.account.creditLimitShared

    fun stats(): List<CreditCurrencyStats> {
        val first = primary.standaloneStats()
        val second = secondary?.standaloneStats()?.copy(limitIsCap = shared) ?: return listOf(first)
        if (!shared) return listOf(first, second)
        val rate = primary.account.creditExchangeRate
            ?.takeIf { it.isFinite() && it > 0.0 }?.toBigDecimal()
            ?: return listOf(first.copy(available = null), second.copy(available = null))
        val remaining = (first.limit.toBigDecimal() - first.toPay.toBigDecimal() -
            second.toPay.toBigDecimal() * rate).max(BigDecimal.ZERO)
        val secondRemaining = (second.limit.toBigDecimal() - second.toPay.toBigDecimal())
            .max(BigDecimal.ZERO)
            .min(remaining.divide(rate, 12, RoundingMode.DOWN))
        return listOf(
            first.copy(
                available = remaining.moneyDown(first.currency),
                estimated = true,
            ),
            second.copy(
                available = secondRemaining.moneyDown(second.currency),
                estimated = true,
            ),
        )
    }
}

private fun BigDecimal.moneyDown(currency: String): Double =
    setScale(IvyCurrency.getDecimalPlaces(currency), RoundingMode.DOWN).toDouble()

private fun AccountData.standaloneStats(): CreditCurrencyStats {
    val owed = (-balance.toBigDecimal()).max(BigDecimal.ZERO)
    val limit = (account.creditLimit ?: 0.0).toBigDecimal()
    return CreditCurrencyStats(
        currency = account.asset.code,
        toPay = owed.toDouble(),
        limit = limit.toDouble(),
        available = (limit - owed).max(BigDecimal.ZERO).moneyDown(account.asset.code),
    )
}

/** Group the two currency ledgers into one physical card, keeping account order. */
fun groupCreditCards(accounts: List<AccountData>): List<CreditCardData> {
    val cards = accounts.filter { it.account.creditLimit != null }
    val roots = cards.filter { !it.account.isSecondaryCreditCurrency }
    val usedIds = mutableSetOf<AccountId>()
    val grouped = roots.map { primary ->
        val secondary = cards.firstOrNull {
            it.account.isSecondaryCreditCurrency &&
                it.account.creditCardGroupId == primary.account.id
        }
        usedIds += primary.account.id
        secondary?.let { usedIds += it.account.id }
        CreditCardData(primary, secondary)
    }
    // Keep imported/orphaned currency accounts visible rather than dropping their debt.
    return grouped + cards.filter { it.account.id !in usedIds }.map { CreditCardData(it) }
}

/** Totals stay in their native currencies: USD and BDT are never added together. */
fun creditCurrencyTotals(cards: List<CreditCardData>): List<CreditCurrencyStats> =
    cards.flatMap { it.stats() }.groupBy { it.currency }.map { (currency, values) ->
        CreditCurrencyStats(
            currency = currency,
            toPay = values.sumOf { it.toPay.toBigDecimal() }.toDouble(),
            limit = values.sumOf { it.limit.toBigDecimal() }.toDouble(),
            available = if (values.any { it.available == null }) null
            else values.sumOf { it.available!!.toBigDecimal() }.toDouble(),
            estimated = values.any { it.estimated },
            limitIsCap = values.any { it.limitIsCap },
        )
    }

data class CreditCardInput(
    val primaryId: AccountId?,
    val name: String,
    val currency: String,
    val limit: Double,
    val color: Int,
    val icon: String?,
    val secondaryCurrency: String?,
    val secondaryLimit: Double?,
    val sharedLimit: Boolean,
    val exchangeRate: Double?,
)

data class CreditCardPaymentInput(
    val cardAccountId: AccountId,
    val fromAccountId: AccountId,
    val paidAmount: Double,
    val debitedAmount: Double,
)
