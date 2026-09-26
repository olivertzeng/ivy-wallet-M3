package com.ivy.legacy.data.model

import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.*
import com.ivy.legacy.domain.validMoney
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class CreditCardDataTest {
    private val root = AccountId(UUID(0, 1))
    private fun ledger(currency: String, limit: Double, owed: Double, secondary: Boolean = false) = AccountData(
        account = Account(
            id = if (secondary) AccountId(UUID(0, 2)) else root,
            name = NotBlankTrimmedString.unsafe("Visa"),
            asset = AssetCode.unsafe(currency), color = ColorInt(0), icon = null,
            includeInBalance = false, orderNum = 0.0, creditLimit = limit, creditCardGroupId = root,
        ), balance = -owed, balanceBaseCurrency = null, monthlyExpenses = 0.0, monthlyIncome = 0.0,
    )
    private fun card(shared: Boolean = true, rate: Double? = 120.0, mainOwed: Double = 20000.0, foreignOwed: Double = 100.0) =
        CreditCardData(
            ledger("BDT", 100000.0, mainOwed).let { it.copy(account = it.account.copy(creditLimitShared = shared, creditExchangeRate = rate)) },
            ledger("USD", 1000.0, foreignOwed, secondary = true),
        )

    @Test fun sharedLimitConsumesBothBalances() {
        val stats = card().stats()
        assertEquals(68000.0, stats[0].available!!, 0.0)
        assertEquals(566.66, stats[1].available!!, 0.0)
        assertEquals(20000.0, stats[0].toPay, 0.0)
        assertEquals(100.0, stats[1].toPay, 0.0)
        assertTrue(stats.all { it.estimated })
    }
    @Test fun independentLimitsDoNotConvertDebt() {
        val stats = card(shared = false).stats()
        assertEquals(80000.0, stats[0].available!!, 0.0)
        assertEquals(900.0, stats[1].available!!, 0.0)
        assertFalse(stats.any { it.estimated })
    }
    @Test fun foreignCapRestrictsSpendingEvenWithOverallRoom() {
        val stats = card(rate = 10.0).stats()
        assertEquals(900.0, stats[1].available!!, 0.0)
    }
    @Test fun exceedingOverallLimitClampsBothAvailableAmounts() {
        assertTrue(card(mainOwed = 100000.0).stats().all { it.available == 0.0 })
    }
    @Test fun exceedingForeignCapDoesNotEraseRemainingMainCredit() {
        val stats = card(rate = 10.0, foreignOwed = 1100.0).stats()
        assertEquals(69000.0, stats[0].available!!, 0.0)
        assertEquals(0.0, stats[1].available!!, 0.0)
    }
    @Test fun missingOrInvalidRateDoesNotInventAvailableCredit() {
        listOf(null, 0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { rate ->
            assertTrue(card(rate = rate).stats().all { it.available == null })
        }
    }
    @Test fun paymentsRestoreSharedCreditInBothViews() {
        val before = card().stats()
        val after = card(foreignOwed = 0.0).stats()
        assertEquals(before[0].available!! + 12000.0, after[0].available!!, 0.0)
        assertEquals(0.0, after[1].toPay, 0.0)
    }
    @Test fun positiveBalancesAreNotDebtOrExtraCredit() {
        val stats = card(mainOwed = -500.0, foreignOwed = 0.0).stats()
        assertEquals(0.0, stats[0].toPay, 0.0)
        assertEquals(100000.0, stats[0].available!!, 0.0)
    }
    @Test fun groupingCountsPhysicalCardsAndKeepsOrphanDebtVisible() {
        val dual = card()
        assertEquals(listOf(dual), groupCreditCards(dual.accounts.reversed()))
        assertEquals(1, groupCreditCards(listOf(dual.secondary!!)).size)
    }
    @Test fun totalsNeverMixCurrencies() {
        val totals = creditCurrencyTotals(listOf(card(), card(shared = false)))
        assertEquals(listOf("BDT", "USD"), totals.map { it.currency })
        assertEquals(40000.0, totals[0].toPay, 0.0)
        assertEquals(200.0, totals[1].toPay, 0.0)
    }
    @Test fun paymentAmountsMustBePositiveFiniteAndCurrencyPrecision() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 1.001).forEach {
            assertFalse(validMoney(it, "BDT"))
        }
        assertTrue(validMoney(12.34, "BDT"))
    }
    @Test fun amountParsingNeverMistakesGroupingForDecimals() {
        assertNull(com.ivy.legacy.domain.parseCreditAmount("1,000", java.util.Locale.US))
        assertEquals(1000.50, com.ivy.legacy.domain.parseCreditAmount("1000.50", java.util.Locale.US)!!, 0.0)
        assertEquals(10.50, com.ivy.legacy.domain.parseCreditAmount("10,50", java.util.Locale.GERMANY)!!, 0.0)
        assertEquals(12.5, com.ivy.legacy.domain.parseCreditAmount("১২.৫", java.util.Locale.US)!!, 0.0)
        assertNull(com.ivy.legacy.domain.parseCreditAmount("NaN"))
        assertNull(com.ivy.legacy.domain.parseCreditAmount("1e3"))
    }
}
