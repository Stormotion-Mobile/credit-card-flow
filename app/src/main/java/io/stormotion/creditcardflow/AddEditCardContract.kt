package io.stormotion.creditcardflow

import io.stormotion.creditcardflow.mvp.BasePresenter
import io.stormotion.creditcardflow.mvp.BaseView

interface AddEditCardContract {
    interface Presenter : BasePresenter {
        fun getCreditCardLogo(creditCardNumber: String)
        fun validateCreditCardNumber(creditCardNumber: String)
        fun validateCreditCardHolder(creditCardHolder: String)
        fun validateCreditCardExpiryDate(creditExpiryDate: String)
        fun validateCreditCardCVV(creditCVV: String)
        fun validateCreditCardTypeAndPriority(creditCardType: String, creditCardPriority: String)
        fun saveCreditCard(number: String,
                           holderName: String,
                           expiryDate: String,
                           cvv: String,
                           type: CardType,
                           isAirplus: Boolean,
                           isPrimary: Boolean)
    }

    interface View : BaseView<Presenter> {
        fun showCreditCardLogo(creditCardEnum: CreditCardEnum)
        fun showNoCreditCardLogo()

        fun showCreditCardNumberValidatedSuccessfully()
        fun showCreditCardNumberFailedToValidate()

        fun showCreditCardHolderValidatedSuccessfully()
        fun showCreditCardHolderFailedToValidate()

        fun showCreditCardExpiryDateValidatedSuccessfully()
        fun showCreditCardExpiryDateFailedToValidate()
        fun showCreditCardExpiryDateIsAlreadyExpired()

        fun showCreditCardCvvValidatedSuccessfully()
        fun showCreditCardCvvFailedToValidate()

        fun showCreditCardPriorityAndTypeValidatedSuccessfully()
        fun showCreditCardPriorityIsEmpty()
        fun showCreditCardTypeIsEmpty()

        fun showCreditCardSavedSuccessfully()
        fun showCreditCardFailedToSave(resourceId: Int)

    }

    enum class CardType {
        PERSONAL, BUSINESS
    }
}