package com.ivy.data.model

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.sync.Identifiable
import com.ivy.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class AccountId(override val value: UUID) : UniqueId

@Suppress("DataClassDefaultValues")
data class Account(
    override val id: AccountId,
    val name: NotBlankTrimmedString,
    val asset: AssetCode,
    val color: ColorInt,
    val icon: IconAsset?,
    val includeInBalance: Boolean,
    override val orderNum: Double,
    val creditLimit: Double? = null,
    /** For a dual-currency card, both accounts point to the primary account's ID. */
    val creditCardGroupId: AccountId? = null,
    val creditLimitShared: Boolean = false,
    /** Units of the primary currency per one unit of the secondary currency. */
    val creditExchangeRate: Double? = null,
) : Identifiable<AccountId>, Reorderable

val Account.isSecondaryCreditCurrency: Boolean
    get() = creditCardGroupId != null && creditCardGroupId != id
