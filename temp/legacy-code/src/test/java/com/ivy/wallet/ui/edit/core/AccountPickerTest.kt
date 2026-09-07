package com.ivy.wallet.ui.edit.core

import com.ivy.legacy.datamodel.Account
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountPickerTest {
    private val cash = Account(name = "Cash", color = 0)
    private val ebl = Account(name = "EBL Bank", currency = "USD", color = 0)
    private val ucb = Account(name = "Ucb Bank", currency = "BDT", color = 0)
    private val accounts = listOf(cash, ebl, ucb)

    @Test
    fun `name search ignores case and surrounding whitespace`() {
        assertEquals(listOf(ebl), filterPickerAccounts(accounts, "  eBl  ", "BDT"))
    }

    @Test
    fun `currency search includes accounts inheriting the wallet currency`() {
        assertEquals(listOf(cash, ucb), filterPickerAccounts(accounts, "bdt", "BDT"))
        assertEquals(listOf(ebl), filterPickerAccounts(accounts, "usd", "BDT"))
    }

    @Test
    fun `clearing search preserves user order and excludes deleted accounts`() {
        val deleted = cash.copy(isDeleted = true)
        assertEquals(accounts, filterPickerAccounts(accounts + deleted, "  ", "BDT"))
    }

    @Test
    fun `duplicate account names remain separately selectable`() {
        val anotherEbl = Account(name = "EBL Bank", currency = "BDT", color = 0)
        assertEquals(
            listOf(ebl, anotherEbl),
            filterPickerAccounts(accounts + anotherEbl, "EBL", "BDT"),
        )
    }

    @Test
    fun `search handles no matches and no accounts`() {
        assertTrue(filterPickerAccounts(accounts, "missing", "BDT").isEmpty())
        assertTrue(filterPickerAccounts(emptyList(), "", "BDT").isEmpty())
    }

    @Test
    fun `search reaches accounts at the end of a large list`() {
        val many = (1..100).map { Account(name = "Account $it", color = 0) }
        assertEquals(listOf(many.last()), filterPickerAccounts(many, "Account 100", "BDT"))
    }
}
