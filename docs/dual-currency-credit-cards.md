# Dual-currency credit cards

The debug build includes the searchable transaction account picker and dual-currency cards.

## Using a card

Enable Credit cards in Settings if the section is hidden. In Accounts, add a card or tap an
existing credit card and choose Edit. Enable Dual-currency card, select its second currency,
and enter its limit.

- Shared overall limit (default for a new dual card): both balances consume the main limit.
  The second limit is a spending cap, not additional credit. Enter the bank rate as main-currency
  units per one second-currency unit. Approximate available amounts share the same overall
  credit and must not be added together.
- Separate limits: each currency's debt reduces only its own limit.

Each currency has its own transaction account. Search/select the currency in the transaction
account picker when recording spending. The Accounts and Home summaries never add unlike
currencies together.

To record a repayment, tap the card, choose the currency and paying account, then enter the
amount credited to the card. For a different paying currency, also enter the actual amount
debited by the bank (including any conversion fees). Partial payments are supported. This
records a transfer; it does not initiate a bank payment.

Just reset is a separately confirmed income adjustment in the selected card currency only.
It does not debit a paying account.

## History and storage

Conversion keeps the existing account ID and transactions and adds a zero-balance secondary
account. Neither currency can be changed or removed after the dual card is saved. Name,
color, icon, limits, limit mode, and bank rate remain editable. Deleting a dual card explicitly
confirms deletion of both currency ledgers and their histories.

Database migration 131 → 132 adds nullable group/rate fields and a false-by-default shared-limit
flag. Both accounts point to the primary account ID. The account list is saved atomically by
Room's list upsert. Full JSON backup/restore preserves the pairing and limit settings; use
JSON for backups. Transaction CSV export/import does not preserve dual-card pairing or
shared-limit settings and is not a complete card backup.

## Checks

Coverage includes shared/separate limits, foreign caps, exhausted limits, missing/invalid rates,
rounding down, native-currency totals, conversion preserving IDs, cross-currency partial
repayments, same-currency transfers, stale overpayments, confirmed reset adjustments,
localized amount parsing, and full JSON backup compatibility.

Paparazzi snapshots cover light/dark card summaries and enlarged text. The host-side
`python3 scripts/verify_dual_currency_migration.py` check validates the migration SQL against
generated schema 132 in in-memory SQLite and checks existing rows are preserved.

No connected Android device or installed emulator was available for an interactive smoke test
or Room's on-device migration test. One existing account-picker snapshot for a pre-scrolled
100-account list remains skipped because of the renderer limitation documented in its test.
