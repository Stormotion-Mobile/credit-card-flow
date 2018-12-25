package io.stormotion.creditcardflow

import android.support.annotation.DrawableRes
import io.stormotion.creditcardflow.removeNotDigits
import java.util.regex.Pattern

enum class CreditCardEnum(val naming: String?, @DrawableRes val cardDrawable: Int?, @DrawableRes val gradientDrawable: Int?, val pattern: String?) {
    VISA("VISA", R.drawable.ic_credit_card_add_edit_visa, R.drawable.credit_card_visa_gradient, "4\\d{12}(?:\\d{3})?"),
    AMERICAN_EXPRESS("AMERICAN EXPRESS", R.drawable.ic_credit_card_add_edit_american_express, R.drawable.credit_card_american_express_diners_gradient, "^3[47][0-9]{13}$"),
    MASTER_CARD("MASTER CARD", R.drawable.ic_credit_card_add_edit_master_card, R.drawable.credit_card_master_card_gradient, "^5[1-5][0-9]{14}$"),
    DISCOVER("DISCOVER", R.drawable.ic_credit_card_add_edit_discover, R.drawable.credit_card_discover_gradient, "^65[4-9][0-9]{13}|64[4-9][0-9]{13}|6011[0-9]{12}|(622(?:12[6-9]|1[3-9][0-9]|[2-8][0-9][0-9]|9[01][0-9]|92[0-5])[0-9]{10})$"),
    DINERS_CLUB_CARD("DINERS CLUB CARD", R.drawable.ic_credit_card_add_edit_diners_club, R.drawable.credit_card_american_express_diners_gradient, "^3(?:0[0-5]|[68][0-9])[0-9]{4,}$"),
    AIRPLUS("AIRPLUS", R.drawable.ic_credit_card_add_edit_airplus, R.drawable.credit_card_airplus_gradient, null),
    UNKNOWN(null, null, null, null);

    companion object {
        fun getCreditCardByNumber(creditCardNumber: String) = values().filter { it.pattern != null }.singleOrNull {
            Pattern.compile(it.pattern).matcher(creditCardNumber.removeNotDigits()).matches()
        } ?: UNKNOWN
    }
}