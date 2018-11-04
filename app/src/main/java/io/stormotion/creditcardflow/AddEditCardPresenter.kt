package io.stormotion.creditcardflow

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.regex.Pattern

class AddEditCardPresenter(val view: AddEditCardContract.View) : AddEditCardContract.Presenter {
    init {
        view.setPresenter(this)
    }

    override fun subscribe() {
    }

    override fun getCreditCardLogo(creditCardNumber: String) {
        if (creditCardNumber.contains("*")) return

        val creditCardEnum = CreditCardEnum.getCreditCardByNumber(creditCardNumber)
        if (creditCardEnum != CreditCardEnum.UNKNOWN) {
            view.showCreditCardLogo(creditCardEnum)
        } else {
            view.showNoCreditCardLogo()
        }
    }

    override fun validateCreditCardNumber(creditCardNumber: String) {
        val creditCardEnum = CreditCardEnum.getCreditCardByNumber(creditCardNumber)
        if (creditCardEnum != CreditCardEnum.UNKNOWN) {
            view.showCreditCardNumberValidatedSuccessfully()
        } else {
            view.showCreditCardNumberFailedToValidate()
        }
    }

    override fun validateCreditCardHolder(creditCardHolder: String) {
        val validCardHolder = Pattern.compile("^((?:[A-Za-z]+ ?){1,3})$").matcher(creditCardHolder).matches()
        if (validCardHolder) {
            view.showCreditCardHolderValidatedSuccessfully()
        } else {
            view.showCreditCardHolderFailedToValidate()
        }
    }

    override fun validateCreditCardExpiryDate(creditExpiryDate: String) {
        val validExpiryDate = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$").matcher(creditExpiryDate).matches()
        if (validExpiryDate) {
            if (checkExpiryDateInTheFuture(creditExpiryDate)) {
                view.showCreditCardExpiryDateValidatedSuccessfully()
            } else {
                view.showCreditCardExpiryDateIsAlreadyExpired()
            }
        } else {
            view.showCreditCardExpiryDateFailedToValidate()
        }
    }

    private fun checkExpiryDateInTheFuture(expiryDate: String): Boolean {
        try {
            val date = SimpleDateFormat("MM/yy").parse(expiryDate)
            if (date.time > System.currentTimeMillis()) {
                return true
            }
        } catch (e: ParseException) {
            return false
        }
        return false
    }


    override fun validateCreditCardCVV(creditCVV: String) {
        val validCVV = Pattern.compile("^[0-9]{3,4}$").matcher(creditCVV).matches()
        if (validCVV) {
            view.showCreditCardCvvValidatedSuccessfully()
        } else {
            view.showCreditCardCvvFailedToValidate()
        }
    }

    override fun validateCreditCardTypeAndPriority(creditCardType: String, creditCardPriority: String) {
        val creditCardTypeValid = creditCardType.isNotEmpty()
        val creditCardPriorityValid = creditCardPriority.isNotEmpty()
        if (!creditCardPriorityValid) {
            view.showCreditCardPriorityIsEmpty()
        }
        if (!creditCardTypeValid) {
            view.showCreditCardTypeIsEmpty()
        }
        if (creditCardPriorityValid && creditCardTypeValid) {
            view.showCreditCardPriorityAndTypeValidatedSuccessfully()
        }
    }

    override fun saveCreditCard(number: String,
                                holderName: String,
                                expiryDate: String,
                                cvv: String,
                                type: AddEditCardContract.CardType,
                                isAirplus: Boolean,
                                isPrimary: Boolean) {
    }
}